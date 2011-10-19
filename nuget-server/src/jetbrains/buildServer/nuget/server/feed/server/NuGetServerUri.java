package jetbrains.buildServer.nuget.server.feed.server;

import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 20:16
 */
public interface NuGetServerUri {
  @Nullable
  String getNuGetFeedBaseUri();
}
