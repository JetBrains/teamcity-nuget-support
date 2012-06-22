/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.agent.runner.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetVersionHolderImpl;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.agent.runner.NuGetRunnerBase;
import jetbrains.buildServer.nuget.agent.runner.install.impl.InstallStagesImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigBuildProcess;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigProcessFactory;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.util.StringUtil;
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
    final InstallStages stages = new InstallStagesImpl(process);
    createStages(context, stages);
    return process;
  }

  private boolean requiresAuthentication(@NotNull NuGetFetchParameters fetch) {
    for (PackageSource src : fetch.getNuGetPackageSources()) {
      if (StringUtil.isEmptyOrSpaces(src.getUserName())) continue;
      if (StringUtil.isEmptyOrSpaces(src.getPassword())) continue;
      return true;
    }
    return false;
  }


  private void createStages(@NotNull final BuildRunnerContext context,
                            @NotNull final InstallStages stages) throws RunBuildException {
    final NuGetFetchParameters parameters = myParametersFactory.loadNuGetFetchParameters(context);
    final PackagesInstallParameters installParameters = myParametersFactory.loadInstallPackagesParameters(context, parameters);
    final PackagesUpdateParameters updateParameters = myParametersFactory.loadUpdatePackagesParameters(context, parameters);

    if (installParameters == null) {
      throw new RunBuildException("NuGet install packages must be enabled");
    }

    final NuGetVersionHolderImpl myVersion = new NuGetVersionHolderImpl();

    stages.getCheckVersionStage().pushBuildProcess(
            myActionFactory.createVersionCheckCommand(
                    context,
                    myVersion,
                    parameters));


    if (requiresAuthentication(parameters)) {
      stages.getAuthenticateStage().pushBuildProcess(
              new DelegatingBuildProcess(new DelegatingBuildProcess.Action() {
                @NotNull
                public BuildProcess startImpl() throws RunBuildException {
                  if (myVersion.getNuGetVerion().supportAuth()) {
                    return myActionFactory.createAuthenticateFeeds(
                            context,
                            parameters.getNuGetPackageSources(),
                            parameters);
                  } else {
                    return new BuildProcessBase() {
                      @NotNull
                      @Override
                      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
                        context.getBuild().getBuildLogger().warning("Current NuGet version does not support feed authentication parameters");
                        return BuildFinishedStatus.FINISHED_SUCCESS;
                      }
                    };
                  }
                }

                public void finishedImpl() {
                }
              }));
    }

    final LocateNuGetConfigBuildProcess locate = myFactory.createPrecess(context, parameters);

    locate.addInstallStageListener(new PackagesInstallerBuilder(
            myActionFactory,
            stages.getInstallStage(),
            context,
            installParameters,
            myVersion));

    if (updateParameters != null) {
      locate.addInstallStageListener(new PackagesUpdateBuilder(
              myActionFactory,
              stages.getUpdateStage(),
              stages.getPostUpdateStart(),
              context,
              installParameters,
              updateParameters));
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
