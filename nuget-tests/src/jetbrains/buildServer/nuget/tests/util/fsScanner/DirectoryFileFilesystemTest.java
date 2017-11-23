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
import jetbrains.buildServer.TestNGUtil;
import jetbrains.buildServer.nuget.agent.util.fsScanner.RealFileSystem;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DirectoryFileFilesystemTest extends BaseTestCase {
  @Test
  public void TestDirectoryRootsWindows() {
    if (!SystemInfo.isWindows) TestNGUtil.skip("test for Windows only");

    DoAbsTest("C:/", true);
    DoAbsTest("C:\\", true);
    DoAbsTest("\\", false);
    DoAbsTest("/", false);
    DoAbsTest("aaa", false);
    DoAbsTest("aaa/vvv", false);
    DoAbsTest("aaa\\vvv", false);
    DoAbsTest("\\aaa\\vvv", false);
    DoAbsTest("/aaa\\vvv", false);
  }

  @Test
  public void TestDirectoryRootUnix() {
    if (SystemInfo.isWindows) TestNGUtil.skip("test for Unix only");

    DoAbsTest("/", true);
    DoAbsTest("aaa/bbb", false);
  }

  private static void DoAbsTest(String path, boolean result) {
    RealFileSystem fs = new RealFileSystem();
    Assert.assertEquals(result, fs.isPathAbsolute(path));
  }
}
