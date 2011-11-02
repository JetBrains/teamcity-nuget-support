package jetbrains.buildServer.nuget.server.feed.server.process;

import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 18:34
 */
public interface NuGetServerRunner {
  void startServer();

  void ensureAlive();

  void stopServer();

  @Nullable
  Integer getPort();
}
