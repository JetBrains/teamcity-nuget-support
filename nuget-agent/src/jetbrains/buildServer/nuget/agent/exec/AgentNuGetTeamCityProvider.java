

package jetbrains.buildServer.nuget.agent.exec;

import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProviderBase;
import org.jetbrains.annotations.NotNull;

/**
 * Created 04.01.13 14:51
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class AgentNuGetTeamCityProvider extends NuGetTeamCityProviderBase implements NuGetTeamCityProvider {
  public AgentNuGetTeamCityProvider(@NotNull final PluginDescriptor pluginInfo) {
    super(pluginInfo.getPluginRoot());
  }
}
