

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.tool.impl.NuGetPackageValidationUtil;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.util.FileUtil;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackageValidationUtilTest extends BaseTestCase {
  @Test
  public void testPackageValidationOldPackage() throws ToolException, IOException {
    File testPackage = createTempFile();
    FileUtil.copy(Paths.getPackagesPath("NuGet.CommandLine.1.8.0/NuGet.CommandLine.1.8.0.nupkg"), testPackage);
    NuGetPackageValidationUtil.validatePackage(testPackage);
  }

  @Test
  public void testPackageValidation() throws ToolException, IOException {
    File testPackage = createTempFile();
    FileUtil.copy(Paths.getPackagesPath("NuGet.CommandLine.3.4.4-rtm-final/NuGet.CommandLine.3.4.4-rtm-final.nupkg"), testPackage);
    NuGetPackageValidationUtil.validatePackage(testPackage);
  }

  @Test(expectedExceptions = ToolException.class)
  public void testPackageValidataionFailed() throws IOException, ToolException {
    NuGetPackageValidationUtil.validatePackage(createTempFile(22233));
  }
}
