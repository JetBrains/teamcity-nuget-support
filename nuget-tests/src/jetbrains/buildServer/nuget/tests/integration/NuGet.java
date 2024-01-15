

package jetbrains.buildServer.nuget.tests.integration;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.nuget.common.version.Version;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 31.08.11 10:39
 */
public enum NuGet {
  NuGet_1_6(1,6),
  NuGet_1_7(1,7),
  NuGet_1_8(1,8),
  NuGet_2_0(2,0),
  NuGet_2_1(2,1),
  NuGet_2_2(2,2),
  NuGet_2_5(2,5),
  NuGet_2_6(2,6),
  NuGet_2_7(2,7),
  NuGet_2_8(2,8),
  NuGet_3_3(3,3),
  NuGet_3_4(3,4),
  NuGet_3_5(3,5),
  NuGet_4_1(4, 1),
  NuGet_4_3(4, 3),
  NuGet_4_8(4,8),
  NuGet_5_8(5,8);

  public final Version version;

  NuGet(int major, int minor) {
    version = new Version(major, minor, 0);
  }

  @NotNull
  public File getPath() {
    switch (this) {
      case NuGet_1_6:
        return Paths.getPackagesPath("NuGet.CommandLine.1.6.0/tools/NuGet.exe");
      case NuGet_1_7:
        return Paths.getPackagesPath("NuGet.CommandLine.1.7.0/tools/NuGet.exe");
      case NuGet_1_8:
        return Paths.getPackagesPath("NuGet.CommandLine.1.8.0/tools/NuGet.exe");
      case NuGet_2_0:
        return Paths.getPackagesPath("NuGet.CommandLine.2.0.40001/tools/NuGet.exe");
      case NuGet_2_1:
        return Paths.getPackagesPath("NuGet.CommandLine.2.1.0/tools/NuGet.exe");
      case NuGet_2_2:
        return Paths.getPackagesPath("NuGet.CommandLine.2.2.0/tools/NuGet.exe");
      case NuGet_2_5:
        return Paths.getPackagesPath("NuGet.CommandLine.2.5.0/tools/NuGet.exe");
      case NuGet_2_6:
        return Paths.getPackagesPath("NuGet.CommandLine.2.6.0/tools/NuGet.exe");
      case NuGet_2_7:
        return Paths.getPackagesPath("NuGet.CommandLine.2.7.0/tools/NuGet.exe");
      case NuGet_2_8:
        return Paths.getPackagesPath("NuGet.CommandLine.2.8.6/tools/NuGet.exe");
      case NuGet_3_3:
        return Paths.getPackagesPath("NuGet.CommandLine.3.3.0/tools/NuGet.exe");
      case NuGet_3_4:
        return Paths.getPackagesPath("NuGet.CommandLine.3.4.4-rtm-final/tools/NuGet.exe");
      case NuGet_3_5:
        return Paths.getPackagesPath("NuGet.CommandLine.3.5.0/tools/NuGet.exe");
      case NuGet_4_1:
        return Paths.getPackagesPath("NuGet.CommandLine.4.1.0/tools/NuGet.exe");
      case NuGet_4_3:
        return Paths.getPackagesPath("NuGet.CommandLine.4.3.0/tools/NuGet.exe");
      case NuGet_4_8:
        return Paths.getPackagesPath("NuGet.CommandLine.4.8.2/tools/NuGet.exe");
      case NuGet_5_8:
        return Paths.getPackagesPath("NuGet.CommandLine.5.8.0/tools/NuGet.exe");
      default:
        throw new IllegalArgumentException("Failed to find nuget " + this);
    }
  }

  private static final Version VERBOSITY_VERSION = new Version(2, 0, 0);

  public void makeOutputVerbose(GeneralCommandLine cmd) {
    if (VERBOSITY_VERSION.compareTo(version) < 0) {
      cmd.addParameter("-Verbosity");
      cmd.addParameter("detailed");
    } else {
      cmd.addParameter("-Verbose");
    }
  }
}
