

package jetbrains.buildServer.nuget.server.settings.impl;

import jetbrains.buildServer.serverSide.ServerPaths;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:39
 */
public class NuGetSettingsManagerConfigurationImpl implements NuGetSettingsManagerConfiguration {
  private final ServerPaths myPaths;

  public NuGetSettingsManagerConfigurationImpl(@NotNull final ServerPaths paths) {
    myPaths = paths;
  }

  @NotNull
  public File getNuGetConfigXml() {
    return new File(myPaths.getConfigDir(), "nuget.xml");
  }
}
