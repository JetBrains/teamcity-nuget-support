

package jetbrains.buildServer.nuget.server.trigger.impl.mode;

import jetbrains.buildServer.nuget.server.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 15:23
 */
public class CheckRequestModeFactory {
  private final CheckRequestModeTeamCity myTeamCityMode = new CheckRequestModeTeamCity();
  private final SystemInfo mySystemInfo;

  public CheckRequestModeFactory(@NotNull final SystemInfo systemInfo) {
    mySystemInfo = systemInfo;
  }

  @NotNull
  public CheckRequestMode createNuGetChecker(@NotNull final File nugetPath) {
    if (mySystemInfo.canStartNuGetProcesses()) return new CheckRequestModeNuGet(nugetPath);
    return createTeamCityChecker();
  }

  @NotNull
  public CheckRequestMode createTeamCityChecker() {
    return myTeamCityMode;
  }
}
