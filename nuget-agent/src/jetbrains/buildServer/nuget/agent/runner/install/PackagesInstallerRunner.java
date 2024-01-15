

package jetbrains.buildServer.nuget.agent.runner.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.agent.runner.NuGetRunnerBase;
import jetbrains.buildServer.nuget.agent.runner.install.impl.InstallStagesImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigBuildProcess;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigProcessFactory;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 13:55
 */
public class PackagesInstallerRunner extends NuGetRunnerBase {
  private final LocateNuGetConfigProcessFactory myFactory;

  public PackagesInstallerRunner(@NotNull final NuGetActionFactory actionFactory,
                                 @NotNull final PackagesParametersFactory parametersFactory,
                                 @NotNull final LocateNuGetConfigProcessFactory factory) {
    super(actionFactory, parametersFactory);
    myFactory = factory;
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    CompositeBuildProcessImpl process = new CompositeBuildProcessImpl();
    InstallStages stages = new InstallStagesImpl(process);
    createStages(context, stages);
    return process;
  }

  private void createStages(@NotNull final BuildRunnerContext context,
                            @NotNull final InstallStages stages) throws RunBuildException {
    final NuGetFetchParameters parameters = myParametersFactory.loadNuGetFetchParameters(context);
    final PackagesInstallParameters installParameters = myParametersFactory.loadInstallPackagesParameters(context, parameters);
    final PackagesUpdateParameters updateParameters = myParametersFactory.loadUpdatePackagesParameters(context, parameters);

    if (installParameters == null) {
      throw new RunBuildException("NuGet install packages must be enabled");
    }

    final LocateNuGetConfigBuildProcess locate = myFactory.createPrecess(context, parameters);

    locate.addInstallStageListener(new PackagesInstallerBuilder(
            myActionFactory,
            stages.getInstallStage(),
            context,
            installParameters));

    if (updateParameters != null) {
      locate.addInstallStageListener(new PackagesUpdateBuilder(
              myActionFactory,
              stages.getUpdateStage(),
              context,
              updateParameters));

      locate.addInstallStageListener(new PackagesPostUpgradeInstallBuilder(
              myActionFactory,
              stages.getPostUpdateStart(),
              context,
              installParameters
      ));
    }

    locate.addInstallStageListener(new PackagesReportBuilder(
      myActionFactory,
      stages.getReportStage(),
      context));

    stages.getLocateStage().pushBuildProcess(locate);
  }

  @NotNull
  public String getType() {
    return PackagesConstants.INSTALL_RUN_TYPE;
  }
}
