

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 14:09
 */
public class LocateNuGetConfigProcessFactory {
  private final RepositoryPathResolver myResolver;
  private final List<PackagesConfigScanner> myScanners;

  public LocateNuGetConfigProcessFactory(@NotNull final RepositoryPathResolver resolver,
                                         @NotNull final List<PackagesConfigScanner> scanners) {
    myResolver = resolver;
    myScanners = scanners;
  }

  @NotNull
  public LocateNuGetConfigBuildProcess createPrecess(@NotNull final BuildRunnerContext context,
                                                     @NotNull final NuGetFetchParameters parameters) {
    return new LocateNuGetConfigBuildProcess(
            parameters,
            context.getBuild().getBuildLogger(),
            myResolver,
            myScanners);
  }

}
