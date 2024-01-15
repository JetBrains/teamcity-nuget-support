

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestMode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 13:56
 */
public class PackageCheckRequest {
  @NotNull private final CheckRequestMode myMode; //way to check version, i.e. java base, nuget base
  @NotNull private final SourcePackageReference myPackage;

  private long myCheckInterval = 5 * 60 * 1000; //5min is default, see jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckerSettings

  public PackageCheckRequest(@NotNull final CheckRequestMode mode,
                             @NotNull final SourcePackageReference aPackage) {
    myMode = mode;
    myPackage = aPackage;
  }

  /**
   * @return updates check interval in milliseconds
   */
  public long getCheckInterval() {
    return myCheckInterval;
  }

  /**
   * Sets update check interval
   * @param checkInterval in milliseconds
   */
  public void setCheckInterval(long checkInterval) {
    myCheckInterval = checkInterval;
  }

  @NotNull
  public CheckRequestMode getMode() {
    return myMode;
  }

  @NotNull
  public SourcePackageReference getPackage() {
    return myPackage;
  }
}
