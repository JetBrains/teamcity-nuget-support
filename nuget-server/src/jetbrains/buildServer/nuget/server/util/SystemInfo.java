

package jetbrains.buildServer.nuget.server.util;

import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 09.11.11 20:57
 */
public interface SystemInfo {
  /**
   * @return true if environment is capable of running NuGet processes and our utils
   */
  boolean canStartNuGetProcesses();

  /**
   * @return true if .NET Framework 4.0 is detected on the machine
   */
  boolean isDotNetFrameworkAvailable();

  /**
   * @return test message describing why it cannot start NuGet processes
   */
  @NotNull
  String getNotAvailableMessage();
}
