

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:44
 */
public class Paths {
  @NotNull
  public static File getTestDataPath() {
    return FileUtil.getCanonicalFile(new File("testData"));
  }

  @NotNull
  public static File getTestDataPath(@NotNull final String p) {
    return FileUtil.getCanonicalFile(new File(getTestDataPath(), p));
  }

  @NotNull
  public static File getPackagesPath(@NotNull final String p) {
    return FileUtil.getCanonicalFile(new File("../nuget-extensions/packages", p));
  }

  @NotNull
  public static File getNuGetRunnerPath() {
    return FileUtil.getCanonicalFile(new File("../nuget-extensions/bin/JetBrains.TeamCity.NuGetRunner.exe"));
  }

  @NotNull
  public static File getCredentialProviderHomeDirectory() {
    return FileUtil.getCanonicalFile(new File("../nuget-extensions/bin/credential-provider"));
  }
}
