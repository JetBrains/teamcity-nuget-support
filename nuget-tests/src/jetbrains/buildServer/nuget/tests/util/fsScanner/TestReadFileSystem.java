/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.tests.util.fsScanner;


import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.agent.util.fsScanner.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestReadFileSystem extends BaseTestCase {
  private interface FilesAction {
    void action(RealDirectoryEntry r1, RealDirectoryEntry r2, RealDirectoryEntry r3, RealDirectoryEntry r4);
  }


  @Test
  public void TestCaseSensitive() {
    Assert.assertEquals(!SystemInfo.isWindows, new RealFileSystem().caseSensitive());
  }

  private void TestFSTest(FilesAction a) throws IOException {

    File path1 = createTempDir();
    File path2 = new File(path1, "aaa");
    File path3 = new File(path2, "bbb");
    File path4 = new File(path3, "ccc");
    path2.mkdirs();
    path3.mkdirs();
    path4.mkdirs();

    RealDirectoryEntry r4 = new RealDirectoryEntry(new FileSystemPath(path4));
    RealDirectoryEntry r3 = new RealDirectoryEntry(new FileSystemPath(path3));
    RealDirectoryEntry r2 = new RealDirectoryEntry(new FileSystemPath(path2));
    RealDirectoryEntry r1 = new RealDirectoryEntry(new FileSystemPath(path1));

    a.action(r1, r2, r3, r4);

  }


  @Test
  public void TestNames() throws IOException {
    TestFSTest(new FilesAction() {
      public void action(RealDirectoryEntry r1, RealDirectoryEntry r2, RealDirectoryEntry r3, RealDirectoryEntry r4) {
        Assert.assertEquals("ccc", r4.getName());
        Assert.assertEquals("bbb", r3.getName());
        Assert.assertEquals("aaa", r2.getName());
      }
    });
  }

  @Test
  public void TestParent() throws IOException {
    TestFSTest(
            new FilesAction() {
              public void action(RealDirectoryEntry r1, RealDirectoryEntry r2, RealDirectoryEntry r3, RealDirectoryEntry r4) {
                AssertDirectoriesEqual(r4.getParent(), r3);
                AssertDirectoriesEqual(r4.getParent().getParent(), r2);
                AssertDirectoriesEqual(r4.getParent().getParent().getParent(), r1);

                AssertDirectoriesEqual(r3.getParent(), r2);
                AssertDirectoriesEqual(r3.getParent().getParent(), r1);

                AssertDirectoriesEqual(r2.getParent(), r1);


              }
            });
  }

  @Test
  public void TestSubdirectories() throws IOException {
    TestFSTest(
            new FilesAction() {
              public void action(RealDirectoryEntry r1, RealDirectoryEntry r2, RealDirectoryEntry r3, RealDirectoryEntry r4) {
                Assert.assertEquals(0, r4.getSubdirectories().length);
                Assert.assertEquals(1, r3.getSubdirectories().length);
                Assert.assertEquals(1, r2.getSubdirectories().length);
                Assert.assertEquals(1, r1.getSubdirectories().length);

                AssertDirectoriesEqual(r3.getSubdirectories()[0], r4);
                AssertDirectoriesEqual(r2.getSubdirectories()[0], r3);
                AssertDirectoriesEqual(r1.getSubdirectories()[0], r2);

              }
            });
  }

  @Test
  public void TestRootSubdirectories() {
    RealRootDirectory root = new RealRootDirectory();
    Assert.assertTrue(root.getSubdirectories().length > 0);
  }

  @Test(enabled = false)
  public void TestRootSubdirectoriesWalk() {
    int k = 10;
    for (DirectoryEntry sub1 : new RealRootDirectory().getSubdirectories()) {
      if (k-- < 0) break;
      int j = 10;
      for (DirectoryEntry sub2 : sub1.getSubdirectories()) {
        if (j-- < 0) break;
        int i = 10;
        for (DirectoryEntry sub3 : sub2.getSubdirectories()) {
          if (i-- < 0) break;
          System.out.println("sub3 = " + sub3);
        }
      }
    }

  }


  @Test
  public void TestRealRoot() {
    DirectoryEntry root = RealFileSystem.ROOT;
    DumpItem(root);
    Assert.assertTrue(root.getSubdirectories().length > 0);
  }

  private static void DumpItem(DirectoryEntry e) {
    Assert.assertNotNull(e);
    System.out.println("entry = " + e);
    for (DirectoryEntry sub : e.getSubdirectories()) {
      System.out.println("entry->sub = " + sub);
    }
    for (FileEntry fileEntry : e.getFiles()) {
      System.out.println("entry->file = " + fileEntry);
    }
  }

  @Test
  public void TestSimpleUpDown() throws IOException {

    File path = new File(createTempDir(), "aaa");
    path.mkdirs();


    DirectoryEntry e = cd(path);
    DirectoryEntry p = e.getParent();
    Assert.assertNotNull(p);

    for (DirectoryEntry dir : p.getSubdirectories()) {
      if (dir.getName().equals(e.getName())) {
        AssertDirectoriesEqual(dir, e);
        return;
      }
    }
    Assert.fail("Failed to find child");
  }


  private static RealDirectoryEntry cd(File path) {
    return new RealDirectoryEntry(new FileSystemPath(path));
  }

  @Test
  public void TestShouldFindRootFS() throws IOException {
    DirectoryEntry r = cd(createTempDir());
    while (true) {
      DirectoryEntry p = r.getParent();
      if (p == null) break;
      r = p;
    }

    Assert.assertNotNull(r);
    Assert.assertEquals(r.getClass(), RealRootDirectory.class);
  }

  @Test
  public void TestWalkUpAndDown() throws IOException {

    File path = createTempDir();
    for (String cc : new String[]{"a", "b", "c", "d", "e", "f", "g", "h"}) {
      path = new File(path, cc);
      path.mkdirs();
    }

    DirectoryEntry test = new RealDirectoryEntry(new FileSystemPath(path));

    List<String> loop = new ArrayList<String>();
    while (true) {
      System.out.println("p = " + test);
      loop.add(test.getName());
      DirectoryEntry p = test.getParent();
      if (p == null) break;
      test = p;
    }
    Collections.reverse(loop);


    DirectoryEntry root = new RealRootDirectory();
    Assert.assertEquals(root.getName(), loop.get(0));
    loop.remove(0);

    for (String name : loop) {
      boolean found = false;
      for (DirectoryEntry sub : root.getSubdirectories()) {
        if (sub.getName().equals(name)) {
          found = true;
          root = sub;
        }
      }
      if (!found) {
        System.out.println("Looking for name '" + name + "{0}'");
        System.out.println("Looking under    '" + root + "'");
        DumpItem(root);

        Assert.assertTrue(found, "Failed to find " + name + " under " + root);
      }
    }

  }

  private static void AssertDirectoriesEqual(DirectoryEntry e1, DirectoryEntry e2) {
    Assert.assertEquals(e1.toString(), e2.toString());
  }

  @Test
  public void TestEquality() throws IOException {
    final File dir = createTempDir();
    AssertDirectoriesEqual(new RealDirectoryEntry(new FileSystemPath(dir)), new RealDirectoryEntry(new FileSystemPath(dir)));
  }

  @Test
  public void TestParentDirectory() throws IOException {
    File path = createTempDir();

    List<DirectoryEntry> entries = new ArrayList<DirectoryEntry>();
    DirectoryEntry e = new RealDirectoryEntry(new FileSystemPath(path));
    while (e.getParent() != null) {
      System.out.println("e = " + e);
      entries.add(e);
      e = e.getParent();
    }
  }
}
