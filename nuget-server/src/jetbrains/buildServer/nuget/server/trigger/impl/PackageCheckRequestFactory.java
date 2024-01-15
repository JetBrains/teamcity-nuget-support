

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestMode;
import jetbrains.buildServer.nuget.server.trigger.impl.settings.PackageCheckerSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 14:02
 */
public class PackageCheckRequestFactory {
  private final PackageCheckerSettings mySettings;

  public PackageCheckRequestFactory(@NotNull final PackageCheckerSettings settings) {
    mySettings = settings;
  }

  @NotNull
  public PackageCheckRequest createRequest(@NotNull final CheckRequestMode mode,
                                           @NotNull final SourcePackageReference reference) {
    final PackageCheckRequest request = new PackageCheckRequest(mode, reference);
    request.setCheckInterval(mySettings.getPackageCheckInterval());
    return request;
  }
}
