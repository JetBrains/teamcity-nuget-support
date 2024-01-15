

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 16:18
 */
public interface PackagesInstallParameters extends NuGetFetchParametersHolder {
  /**
   * @return true if pacakges are expected to be installed
   *         without version
   *         numbers in directory names
   */
  boolean getExcludeVersion();
  boolean getNoCache();


  @NotNull
  PackagesInstallMode getInstallMode() throws RunBuildException;
}
