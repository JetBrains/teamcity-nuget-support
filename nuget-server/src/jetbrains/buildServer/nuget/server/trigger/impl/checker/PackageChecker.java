

package jetbrains.buildServer.nuget.server.trigger.impl.checker;

import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 16:37
 */
public interface PackageChecker {

  boolean accept(@NotNull PackageCheckRequest request);

  /**
   * Implementation should schedule update of given type as a task in the executor
   * @param executor executor to perform work
   * @param entries requests to check
   */
  void update(@NotNull ExecutorService executor, @NotNull Collection<CheckablePackage> entries);
}
