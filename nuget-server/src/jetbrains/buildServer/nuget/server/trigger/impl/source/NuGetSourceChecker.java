

package jetbrains.buildServer.nuget.server.trigger.impl.source;

import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.07.12 13:09
 * @since 7.1
 */
public interface NuGetSourceChecker {
  /**
   * Checks all package sources from the collection.
   * Mehod returns only packages with sources that are accessible.
   * All other sources will be updated by the checker.
   *
   * @param allPackages all packages to check
   * @return only accessible packages
   */
  @NotNull
  Collection<CheckablePackage> getAccessiblePackages(@NotNull Collection<CheckablePackage> allPackages);
}
