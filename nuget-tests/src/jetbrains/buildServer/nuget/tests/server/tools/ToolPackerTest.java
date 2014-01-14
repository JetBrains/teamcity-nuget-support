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

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolPacker;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 1:50
 */
public class ToolPackerTest extends BaseTestCase {
  private ToolPacker myPacker;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myPacker = new ToolPacker();
  }

  @Test
  public void testPack() throws IOException {
    final File root = createTempDir();
    final File plugin = createTempFile();
    myPacker.packTool(plugin, root);

    assertZip(plugin, "teamcity-plugin.xml");
  }

  @Test
  public void testPack_oneFile() throws IOException {
    final File root = createTempDir();
    FileUtil.writeFile(new File(root, "aaa.txt"), "content");
    final File plugin = createTempFile();
    myPacker.packTool(plugin, root);


    assertZip(plugin, "teamcity-plugin.xml", "aaa.txt");
  }

  @Test
  public void testPack_oneFile2() throws IOException {
    final File root = createTempDir();
    FileUtil.writeFile(new File(root, "a/b/c/d/aaa.txt"){{getParentFile().mkdirs();}}, "content");
    final File plugin = createTempFile();
    myPacker.packTool(plugin, root);

    assertZip(plugin, "teamcity-plugin.xml", "a/b/c/d/aaa.txt");
  }

  private void assertZip(@NotNull final File zip, @NotNull String... paths) throws IOException {
    Set<String> pathsToContain = new TreeSet<String>(Arrays.asList(paths));
    Set<String> actualPaths = new TreeSet<String>();
    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
    try {
      ZipEntry e;
      while((e = zis.getNextEntry()) != null) {
        pathsToContain.remove(e.getName());
        actualPaths.add(e.getName());
      }
    } finally {
      FileUtil.close(zis);
    }

    Assert.assertTrue(pathsToContain.isEmpty(), "Should contain: " + pathsToContain + ", but was: " + actualPaths);
  }


}
