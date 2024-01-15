

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.tool.ToolIdUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Evgeniy.Koshkin
 */
public class ToolIdUtilsTest extends BaseTestCase {

  @DataProvider
  public Object[][] packageNames() {
    return new Object[][]{
      {"NuGet.CommandLine.1.2.3", "1.2.3"},
      {"NuGet.CommandLine.2.3.4.5", "2.3.4.5", },
      {"NuGet.CommandLine.2.3.4.5-alpha", "2.3.4.5-alpha"},
      {"win-x86-commandline.3.2.0", "win-x86-commandline.3.2.0"},
      {"win-x86-commandline.3.2.0-rc", "win-x86-commandline.3.2.0-rc"},
      {"NuGet.Hack.2.3.4.5-alpha", "NuGet.Hack.2.3.4.5-alpha"},
      {"nuget.commandline.1.2.3", "1.2.3"},
      {"NUGET.COMMANDLINE.1.2.3", "1.2.3"},
      {"NuGet.CommandLine.4.8.0-rtm.5369+9f75ce12e9d1e153c7a087f6d01cc09861effe26", "4.8.0-rtm.5369"},
    };
  }

  @Test(dataProvider = "packageNames")
  public void testGetPackageVersion(final String packageName, final String expectedName) {
    assertEquals(expectedName, ToolIdUtils.getPackageVersion(packageName));
  }

  @DataProvider
  public Object[][] packageIds() {
    return new Object[][]{
      {"NuGet.CommandLine.1.2.3", "NuGet.CommandLine.1.2.3"},
      {"NuGet.CommandLine.2.3.4.5-alpha", "NuGet.CommandLine.2.3.4.5-alpha"},
      {"win-x86-commandline.3.2.0", "win-x86-commandline.3.2.0"},
      {"NuGet.Hack.2.3.4.5-alpha", "NuGet.Hack.2.3.4.5-alpha"},
      {"nuget.commandline.1.2.3", "NuGet.CommandLine.1.2.3"},
      {"NUGET.COMMANDLINE.1.2.3", "NuGet.CommandLine.1.2.3"},
      {"NuGet.CommandLine.4.8.0-rtm.5369+9f75ce12e9d1e153c7a087f6d01cc09861effe26", "NuGet.CommandLine.4.8.0-rtm.5369"},
    };
  }

  @Test(dataProvider = "packageIds")
  public void testGetPackageId(final String packageName, final String expectedName) {
    assertEquals(expectedName, ToolIdUtils.getPackageId(packageName));
  }
}
