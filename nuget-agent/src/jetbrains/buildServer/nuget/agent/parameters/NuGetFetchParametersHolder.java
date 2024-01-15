

package jetbrains.buildServer.nuget.agent.parameters;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.07.11 15:57
 */
public interface NuGetFetchParametersHolder {
  @NotNull
  NuGetFetchParameters getNuGetParameters();
}
