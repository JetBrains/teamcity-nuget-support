

package jetbrains.buildServer.nuget.common.exec;

import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created 04.01.13 16:22
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetTeamCityProviderBase implements NuGetTeamCityProvider {
  @NotNull
  private final File myNugetBinariesRoot;

  public NuGetTeamCityProviderBase(@NotNull final File nugetBinariesRoot) {
    myNugetBinariesRoot = nugetBinariesRoot;
  }

  @NotNull
  public final File getNuGetRunnerPath() {
    return getCanonicalFile("bin/JetBrains.TeamCity.NuGetRunner.exe");
  }

  @NotNull
  @Override
  public String getPluginCorePath(int minSdkVersion) {
    if(minSdkVersion <= 3) {
      return getCanonicalFile("/bin/credential-plugin/netcoreapp" + minSdkVersion + ".0/CredentialProvider.TeamCity.dll").getPath();
    }

    return getCanonicalFile("/bin/credential-plugin/net" + minSdkVersion + ".0/CredentialProvider.TeamCity.dll").getPath();
  }

  @NotNull
  @Override
  public String getPluginFxPath() {
    return getCanonicalFile("/bin/credential-plugin/net46/CredentialProvider.TeamCity.exe").getPath();
  }

  @NotNull
  public File getCredentialProviderHomeDirectory() {
    return getCanonicalFile("bin/credential-provider");
  }

  private File getCanonicalFile(final String path) {
    return FileUtil.getCanonicalFile(new File(myNugetBinariesRoot, path));
  }
}
