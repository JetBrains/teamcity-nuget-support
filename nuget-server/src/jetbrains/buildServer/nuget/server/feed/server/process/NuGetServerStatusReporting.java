package jetbrains.buildServer.nuget.server.feed.server.process;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 18:33
 */
public interface NuGetServerStatusReporting {
  void startingServer();

  void stoppingServer();

  void pingSucceeded();

  void pingFailed();

  void setRunning();
}
