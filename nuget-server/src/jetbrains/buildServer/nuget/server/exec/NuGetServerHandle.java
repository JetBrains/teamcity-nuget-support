

package jetbrains.buildServer.nuget.server.exec;

/**
 * Running TeamCity NuGet feed process
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 14:42
 */
public interface NuGetServerHandle {
  int getPort();

  boolean isAlive();

  void stop();
}
