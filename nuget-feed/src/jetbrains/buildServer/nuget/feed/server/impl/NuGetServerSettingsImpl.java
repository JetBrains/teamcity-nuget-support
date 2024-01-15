

package jetbrains.buildServer.nuget.feed.server.impl;

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 17.01.12 17:14
 */
public class NuGetServerSettingsImpl implements NuGetServerSettings {

  public NuGetServerSettingsImpl() {
  }

  public boolean isNuGetServerEnabled() {
    return TeamCityProperties.getBooleanOrTrue(NuGetFeedConstants.PROP_NUGET_FEED_ENABLED);
  }

  public boolean isFilteringByTargetFrameworkEnabled() {
    return TeamCityProperties.getBoolean(NuGetFeedConstants.PROP_NUGET_FEED_FILTER_TARGETFRAMEWORK);
  }
}
