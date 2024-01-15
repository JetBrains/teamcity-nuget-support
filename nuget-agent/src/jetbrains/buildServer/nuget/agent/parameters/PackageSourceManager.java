

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Created 04.01.13 19:18
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface PackageSourceManager {
  /**
   * @param build running build
   * @return returns collection of all build-wide package sources
   */
  @NotNull
  Set<PackageSource> getGlobalPackageSources(@NotNull AgentRunningBuild build);
}
