

package jetbrains.buildServer.nuget.server.trigger.impl.checker;

import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestMode;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestModeNuGet;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 20:15
 */
public class PackageCheckerNuGetBase {
  public boolean accept(@NotNull PackageCheckRequest request) {
    return (request.getMode() instanceof CheckRequestModeNuGet);
  }

  @NotNull
  protected File getNuGetPath(@NotNull CheckRequestMode entry) {
    return ((CheckRequestModeNuGet)entry).getNuGetPath();
  }
}
