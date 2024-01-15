

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestMode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 20:40
 */
public interface CheckablePackage {
  @NotNull
  CheckRequestMode getMode();

  @NotNull
  SourcePackageReference getPackage();

  void setExecuting();

  void setResult(@NotNull CheckResult result);
}
