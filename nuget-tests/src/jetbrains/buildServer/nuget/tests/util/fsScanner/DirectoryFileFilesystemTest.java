

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
