

package jetbrains.buildServer.nuget.agent.runner.publish;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.runner.NuGetRunnerBase;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CompositeBuildProcess;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 15:15
 */
public class PackagesPublishRunner extends NuGetRunnerBase {
  public PackagesPublishRunner(@NotNull final NuGetActionFactory actionFactory,
                               @NotNull final PackagesParametersFactory parametersFactory) {
    super(actionFactory, parametersFactory);
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    final CompositeBuildProcess process = new CompositeBuildProcessImpl();
    final NuGetPublishParameters params = myParametersFactory.loadPublishParameters(context);

    process.pushBuildProcess(new MatchFilesBuildProcess(context, params, new MatchFilesBuildProcess.Callback() {
      public void fileFound(@NotNull final File file) throws RunBuildException {
        final CompositeBuildProcess composite = new CompositeBuildProcessImpl();
        composite.pushBuildProcess(new BuildProcessBase() {
          @NotNull
          @Override
          protected BuildFinishedStatus waitForImpl() throws RunBuildException {
            if(FeedConstants.SYMBOLS_PACKAGE_FILE_FILTER.accept(file)){
              context.getBuild().getBuildLogger().warning("Attempt to publish symbol package. " +
                      "Symbol packages are not fully supported by TeamCity internal feed. " +
                      "For more details see https://confluence.jetbrains.com/display/TCDL/NuGet#NuGet-symbols");
            } else if (!FeedConstants.PACKAGE_FILE_FILTER.accept(file)) {
              context.getBuild().getBuildLogger().warning(
                      "Attempt to publish NuGet package with wrong extension: "
                              + "." + FileUtil.getExtension(file.getPath())
                              + ", expected: " + FeedConstants.NUGET_EXTENSION
              );
            }
            return BuildFinishedStatus.FINISHED_SUCCESS;
          }
        });
        composite.pushBuildProcess(myActionFactory.createPush(context, params, file));
        composite.pushBuildProcess(myActionFactory.createPublishedPackageReport(context, params, file));
        process.pushBuildProcess(composite);
      }
    }));

    return process;
  }

  @NotNull
  public String getType() {
    return PackagesConstants.PUBLISH_RUN_TYPE;
  }
}
