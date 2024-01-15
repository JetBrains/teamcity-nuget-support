

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.nuget.server.trigger.impl.CheckResult;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 14:01
 */
public interface PackageChangesManager {
  /**
   * Registers package for check and returns start reason if change were detected
   * @param request check request
   * @return return version string of current plugin
   */
  @Nullable
  CheckResult checkPackage(@NotNull PackageCheckRequest request);
}
