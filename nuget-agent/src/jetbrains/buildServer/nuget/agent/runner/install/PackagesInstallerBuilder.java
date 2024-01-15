

package jetbrains.buildServer.nuget.agent.runner.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.07.11 14:57
 */
public class PackagesInstallerBuilder extends PackagesInstallerAdapter {
  private final NuGetActionFactory myActionFactory;
  private final BuildProcessContinuation myStages;
  protected final BuildRunnerContext myContext;

  private final PackagesInstallParameters myInstallParameters;

  public PackagesInstallerBuilder(@NotNull final NuGetActionFactory actionFactory,
                                  @NotNull final BuildProcessContinuation stages,
                                  @NotNull final BuildRunnerContext context,
                                  @NotNull final PackagesInstallParameters installParameters) {
    myStages = stages;
    myContext = context;
    myInstallParameters = installParameters;
    myActionFactory = actionFactory;
  }

  @Override
  public void onSolutionFileFound(@NotNull File sln, @NotNull File repositoryPath) throws RunBuildException {
    if (myInstallParameters.getInstallMode() != PackagesInstallMode.VIA_RESTORE) return;
    myStages.pushBuildProcess(myActionFactory.createRestoreForSolution(myContext, myInstallParameters, sln));
  }

  public void onPackagesConfigFound(@NotNull final File config, @NotNull final File repositoryPath) throws RunBuildException {
    if (myInstallParameters.getInstallMode() != PackagesInstallMode.VIA_INSTALL) return;

    myStages.pushBuildProcess(wrapConfigProcess(config, new BuildProcessFactory() {
      @NotNull
      public BuildProcess createBuildProcess() throws RunBuildException {
        return myActionFactory.createInstall(
                myContext,
                myInstallParameters,
                config,
                repositoryPath);
      }
    }));
  }

  @Override
  public void onNoPackagesConfigsFound() throws RunBuildException {
    if (myInstallParameters.getInstallMode() != PackagesInstallMode.VIA_INSTALL) return;
    myContext.getBuild().getBuildLogger().warning("No packages.config files were found under solution. Nothing to install");
  }

  @NotNull
  protected BuildProcess wrapConfigProcess(@NotNull final File config, @NotNull final BuildProcessFactory proc) throws RunBuildException {
    return proc.createBuildProcess();
  }

  protected interface BuildProcessFactory {
    @NotNull
    BuildProcess createBuildProcess() throws RunBuildException;
  }
}
