

package jetbrains.buildServer.nuget.agent.runner.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created 13.08.13 13:47
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class PackagesPostUpgradeInstallBuilder extends PackagesInstallerBuilder {
  public PackagesPostUpgradeInstallBuilder(@NotNull final NuGetActionFactory actionFactory,
                                           @NotNull final BuildProcessContinuation stages,
                                           @NotNull final BuildRunnerContext context,
                                           @NotNull final PackagesInstallParameters installParameters) {
    super(actionFactory, stages, context, installParameters);
  }

  @NotNull
  @Override
  protected BuildProcess wrapConfigProcess(@NotNull final File config, @NotNull final BuildProcessFactory proc) {
    return new DelegatingBuildProcess(new DelegatingBuildProcess.Action() {
      @NotNull
      public BuildProcess startImpl() throws RunBuildException {
        if (config.isFile()) {
          return PackagesPostUpgradeInstallBuilder.super.wrapConfigProcess(config, proc);
        } else {
          return new BuildProcessBase() {
            @NotNull
            @Override
            protected BuildFinishedStatus waitForImpl() throws RunBuildException {
              BuildProgressLogger log = myContext.getBuild().getBuildLogger();
              log.warning("Packages.config file was removed by NuGet.exe update command. See http://nuget.codeplex.com/workitem/2017 for details");
              return BuildFinishedStatus.FINISHED_SUCCESS;
            }
          };
        }
      }

      public void finishedImpl() {
      }
    });
  }
}
