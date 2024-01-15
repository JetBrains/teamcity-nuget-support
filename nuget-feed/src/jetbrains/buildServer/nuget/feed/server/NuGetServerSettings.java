

package jetbrains.buildServer.nuget.feed.server;

import static jetbrains.buildServer.nuget.common.FeedConstants.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 18:53
 */
public interface NuGetServerSettings {

  String PATH_PREFIX = NUGET_PATH_PREFIX;
  String DEFAULT_PATH_SUFFIX = "/v1/FeedService.svc";
  String DEFAULT_PATH = PATH_PREFIX + DEFAULT_PATH_SUFFIX;
  String PROJECT_PATH = PATH_PREFIX + NUGET_PROJECT_PATH_SUFFIX;
  String SERVICE_FEED_PATH = PROJECT_PATH + NUGET_SERVICE_FEED_PATH_SUFFIX;

  /**
   * @return true if any of NuGet server implementations are enabled
   */
  boolean isNuGetServerEnabled();

  /**
   * @return true if feed filters responses by target framework requested
   */
  boolean isFilteringByTargetFrameworkEnabled();
}
