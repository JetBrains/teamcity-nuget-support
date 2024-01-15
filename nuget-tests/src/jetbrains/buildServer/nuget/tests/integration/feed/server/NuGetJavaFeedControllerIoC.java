

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedProvider;

/**
 * @author Dmitry.Tretyakov
 *         Date: 10.08.2016
 *         Time: 2:08
 */
public final class NuGetJavaFeedControllerIoC {

  private static NuGetFeedProvider ourFeedProvider;

  public static void setFeedProvider(final NuGetFeedProvider feedProvider){
    ourFeedProvider = feedProvider;
  }

  public static NuGetFeedProvider getFeedProvider() {
    return ourFeedProvider;
  }
}
