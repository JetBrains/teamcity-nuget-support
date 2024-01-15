

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Contains settings for packages update parameters
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.07.11 14:02
 */
public interface PackagesUpdateParameters extends NuGetFetchParametersHolder {

  /**
   * @return the way packages are updated
   */
  @NotNull
  PackagesUpdateMode getUpdateMode();

  /**
   * @return true if update should be performed
   *         with -Safe argument
   */
  boolean getUseSafeUpdate();

  /**
   * @return true if update should include prerelease packages
   */
  boolean getIncludePrereleasePackages();

  /**
   * @return list of package Ids to update. Empty list
   *         means update all packages
   */
  @NotNull
  Collection<String> getPackagesToUpdate();

  @NotNull
  Collection<String> getCustomCommandline();
}
