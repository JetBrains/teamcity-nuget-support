

package jetbrains.buildServer.nuget.agent.runner.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static jetbrains.buildServer.nuget.common.PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 12:10
 */
public class PackagesUpdateBuilder extends PackagesInstallerAdapter {
  private final NuGetActionFactory myActionFactory;
  private final BuildProcessContinuation myUpdateStages;
  private final BuildRunnerContext myContext;
  private final PackagesUpdateParameters myUpdateParameters;

  public PackagesUpdateBuilder(@NotNull final NuGetActionFactory actionFactory,
                               @NotNull final BuildProcessContinuation updateStages,
                               @NotNull final BuildRunnerContext context,
                               @NotNull final PackagesUpdateParameters updateParameters) {
    myContext = context;
    myUpdateStages = updateStages;
    myUpdateParameters = updateParameters;
    myActionFactory = actionFactory;
  }

  public void onSolutionFileFound(@NotNull File sln, @NotNull File repositoryPath) throws RunBuildException {
    super.onSolutionFileFound(sln, repositoryPath);

    if (myUpdateParameters.getUpdateMode() != PackagesUpdateMode.FOR_SLN) return;

    myUpdateStages.pushBuildProcess(
            myActionFactory.createUpdate(
                    myContext,
                    myUpdateParameters,
                    sln,
                    repositoryPath
            )
    );
  }

  public void onPackagesConfigFound(@NotNull final File config, @NotNull final File repositoryPath) throws RunBuildException {
    super.onPackagesConfigFound(config, repositoryPath);

    if (myUpdateParameters.getUpdateMode() != FOR_EACH_PACKAGES_CONFIG) return;
    myUpdateStages.pushBuildProcess(
            myActionFactory.createUpdate(
                    myContext,
                    myUpdateParameters,
                    config,
                    repositoryPath
            )
    );
  }
}
