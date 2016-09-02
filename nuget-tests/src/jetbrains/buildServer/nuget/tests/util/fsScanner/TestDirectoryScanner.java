/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.agent.util.fsScanner.DirectoryScanner;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.TestFor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestDirectoryScanner extends BaseTestCase {
  private void AssertScannerResult(String[] fsDescription, String[] includePatterns, String[] excludePatterns, String[] expectedResult) throws IOException {
    File fsp = createTempDir();

    CreateDirectories(fsDescription, fsp);
    Collection<File> findFiles = DirectoryScanner.findFiles(fsp, Arrays.asList(includePatterns), Arrays.asList(excludePatterns));

    Set<File> expected = new TreeSet<File>();
    for (String s : expectedResult) {
      expected.add(new File(fsp, s));
    }
    Set<File> actual = new TreeSet<File>();

    System.out.println("Found: ");
    for (File file : findFiles) {
      actual.add(file);
      System.out.println("   " + file);
    }

    Assert.assertEquals(expected.size(), actual.size());
    final Iterator<File> eIt = expected.iterator();
    final Iterator<File> aIt = actual.iterator();

    for (int i = 0; i < expected.size(); i++) {
      final File eNext = eIt.next();
      final File aNext = aIt.next();
      Assert.assertEquals(PreparePath(eNext), PreparePath(aNext));
    }
  }


  private static void CreateDirectories(String[] fsDescription, File fsp) {
    for (String f : fsDescription) {
      File path = new File(fsp, f.substring(2));
      if (f.startsWith("f:")) {
        FileUtil.createParentDirs(path);
        FileUtil.writeFile(path, "text");
      } else if (f.startsWith("d:")) {
        path.mkdirs();
      } else {
        Assert.fail("Wrong fsDescription: " + f);
      }
    }
  }


  @Test
  public void TestSmoke() throws IOException {
    AssertScannerResult(
            new String[]
                    {
                            "d:a",
                            "f:a/a.exe",
                            "f:a/a.dll",
                            "f:a/a.ca",
                    },
            new String[]{"**"},
            new String[]{"**/*.exe"},
            new String[]{"a/a.ca", "a/a.dll"});
  }

  @Test
  public void TestEmptyExclude() throws IOException {
    AssertScannerResult(
            new String[]
                    {
                            "d:a",
                            "f:a/a.exe",
                            "f:a/a.dll",
                            "f:a/a.ca",
                    },
            new String[]{"**"},
            new String[]{},
            new String[]{"a/a.ca", "a/a.dll", "a/a.exe"});
  }

  @Test
  public void TestEmptyInclude() throws IOException {
    AssertScannerResult(
            new String[]
                    {
                            "d:a",
                            "f:a/a.exe",
                            "f:a/a.dll",
                            "f:a/a.ca",
                    },
            new String[]{},
            new String[]{"**"},
            new String[]{});
  }

  @Test
  public void TestBothEmpty() throws IOException {
    AssertScannerResult(
            new String[]
                    {
                            "d:a",
                            "f:a/a.exe",
                            "f:a/a.dll",
                            "f:a/a.ca",
                    },
            new String[]{},
            new String[]{},
            new String[]{});
  }

  @Test
  public void TestShouldNotMatchDirectory() throws IOException {
    AssertScannerResult(
            new String[]
                    {
                            "f:a/b/c/d/e/f/g/h/i/p/g/aaa.txt",
                            "d:a/r/c/d/e/f/g/h/i/p/g/aaa.txt",
                    },
            new String[]{"**/*.txt"},
            new String[]{},
            new String[]{"a/b/c/d/e/f/g/h/i/p/g/aaa.txt"});
  }

  @Test
  public void TestCaseSensitive() throws IOException {
    if (SystemInfo.isWindows) return;

    AssertScannerResult(
            new String[]
                    {
                            "f:a/b/c/d/e/f/g/h/i/p/g/aAa.txt",
                            "f:a/r/e/a/l/f/g/h/i/p/g/aaa.txt",
                    },
            new String[]{"**/*A*.txt"},
            new String[]{},
            new String[]{"a/b/c/d/e/f/g/h/i/p/g/aAa.txt"});
  }

  @Test
  public void TestCaseInSensitive() throws IOException {
    if (!SystemInfo.isWindows) return;

    AssertScannerResult(
            new String[]
                    {
                            "f:a/b/c/d/e/f/g/h/i/p/g/aAa.txt",
                            "f:a/r/e/a/l/f/g/h/i/p/g/aaa.txt",
                    },
            new String[]{"**/*A*.txt"},
            new String[]{},
            new String[]
                    {
                            "a/b/c/d/e/f/g/h/i/p/g/aAa.txt",
                            "a/r/e/a/l/f/g/h/i/p/g/aaa.txt",
                    });
  }


  @Test
  @TestFor(issues = "TW-20436")
  public void testManyPatterns() throws IOException {
    final File fsp = createTempDir();

    Collection<File> files = DirectoryScanner.findFiles(fsp, Arrays.asList(
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/h/hg/f/d/r/f/d/r/g/d",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/h/hg/f/d/r/f/d/E/g/d",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/h/hh/f/d/r/f/d/E/g/d",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/h/hh/frd/r/f/d/E/g/d",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/hh/frd/r/f/d/E/g/d",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/t/g/d/r/t/hh/frd/r/f/d/E/g/d",
            "a/b/v/d/e/r/g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g/d",
            "a/b/v/d//g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g/d",
            "a/b/v/d/Ui/g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g/d",
            "a/b/v/d/g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g/d",
            "a/b/v/d/g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g/d",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/h/hg/f/d/r/f/d/r/g/d",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/h/hg/f/d/r/f/d/E/g/d",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/h/hh/f/d/r/f/d/E/g/dd",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/h/hh/frd/r/f/d/E/g/dd",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/e/t/g/d/r/t/hh/frd/r/f/d/E/g/dd",
            "a/b/v/d/e/r/g/f/g/h/t/g/d/t/g/d/r/t/hh/frd/r/f/d/E/g/dd",
            "a/b/v/d/e/r/g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g/d/f/d/d",
            "a/b/v/d//g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g//e/e/d/e/sdd",
            "a/b/v/d/Ui/g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g/d/e/t/d/d",
            "a/b/v/d/g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g//e/dc/r/d",
            "a/b/v/d/g/f/g/h/t/d/t/g/d/r/t/hh/frd/r/f/d/E/g/d/e/r/t/yt/d"),
            Collections.<String>emptyList());
    Assert.assertEquals(0, files.size());
  }

  @Test
  public void AbsoluteIncludePath() throws IOException {

    File fsp = createTempDir();
    File atxtFsp = new File(fsp, "a.txt");
    FileUtil.writeFile(atxtFsp, "text");

    Collection<File> files = DirectoryScanner.findFiles(fsp, Arrays.asList(atxtFsp.getPath()), Collections.<String>emptyList());
    Assert.assertEquals(1, files.size());
    Assert.assertEquals(PreparePath(atxtFsp), PreparePath(files.iterator().next()));
  }

  private static String PreparePath(File f) {
    String path = f.getPath();
    path = path.replace('\\', '/');
    if (!SystemInfo.isWindows) {
      return path;
    } else {
      return path.toLowerCase();
    }
  }
}
