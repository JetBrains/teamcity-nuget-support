

package jetbrains.buildServer.nuget.agent.runner.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 12:11
 */
public class PackagesReportBuilder extends PackagesInstallerAdapter {
  private final NuGetActionFactory myActionFactory;
  private final BuildProcessContinuation myStages;
  private final BuildRunnerContext myContext;

  public PackagesReportBuilder(@NotNull final NuGetActionFactory actionFactory,
                               @NotNull final BuildProcessContinuation stages,
                               @NotNull final BuildRunnerContext context) {
    myStages = stages;
    myContext = context;
    myActionFactory = actionFactory;
  }

  public final void onPackagesConfigFound(@NotNull final File config, @NotNull final File repositoryPath) throws RunBuildException {
    myStages.pushBuildProcess(myActionFactory.createUsageReport(myContext, config));
  }
}
