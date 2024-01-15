

package jetbrains.buildServer.nuget.server.exec.impl;

import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProviderBase;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:47
 */
public class ServerNuGetTeamCityProviderImpl extends NuGetTeamCityProviderBase {
  public ServerNuGetTeamCityProviderImpl(@NotNull final PluginDescriptor pluginInfo) {
    super(pluginInfo.getPluginRoot());
  }
}
