

package jetbrains.buildServer.nuget.common.exec;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:49
 */
public interface NuGetTeamCityProvider {
  @NotNull File getNuGetRunnerPath();
  @NotNull File getCredentialProviderHomeDirectory();
  @NotNull String getPluginFxPath();
  @NotNull String getPluginCorePath(int minSdkVersion);
}
