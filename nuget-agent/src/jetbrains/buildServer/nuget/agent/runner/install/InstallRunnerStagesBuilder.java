/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetVersionHolderImpl;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.install.impl.builders.PackagesInstallerBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.impl.builders.PackagesReportBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.impl.builders.PackagesUpdateBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigBuildProcess;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigProcessFactory;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 25.06.12 14:16
 */
public class InstallRunnerStagesBuilder {
  private final NuGetActionFactory myActionFactory;
  private final LocateNuGetConfigProcessFactory myFactory;

  public InstallRunnerStagesBuilder(@NotNull NuGetActionFactory actionFactory,
                                    @NotNull LocateNuGetConfigProcessFactory configFactory) {
    myActionFactory = actionFactory;
    myFactory = configFactory;
  }

  public void buildStages(@NotNull final InstallStages stages,
                          @NotNull final BuildRunnerContext context,
                          @NotNull final NuGetFetchParameters parameters,
                          @NotNull final PackagesInstallParameters installParameters,
                          @Nullable final PackagesUpdateParameters updateParameters) throws RunBuildException {

    final NuGetVersionHolderImpl myVersion = new NuGetVersionHolderImpl();

    stages.getCheckVersionStage().pushBuildProcess(
            myActionFactory.createVersionCheckCommand(
                    context,
                    myVersion,
                    parameters));

    if (requiresAuthentication(parameters)) {
      stages.getAuthenticateStage().pushBuildProcess(createAuthProcess(context, parameters, myVersion));
    }

    final LocateNuGetConfigBuildProcess locate = myFactory.createPrecess(context, parameters);

    locate.addInstallStageListener(new PackagesInstallerBuilder(
            myActionFactory,
            stages,
            context,
            installParameters,
            myVersion));

    if (updateParameters != null) {
      locate.addInstallStageListener(new PackagesUpdateBuilder(
              myActionFactory,
              stages,
              context,
              myVersion,
              installParameters,
              updateParameters));
    }

    locate.addInstallStageListener(new PackagesReportBuilder(
            myActionFactory,
            stages,
            context)
    );

    stages.getLocateStage().pushBuildProcess(locate);
  }

  private DelegatingBuildProcess createAuthProcess(@NotNull final BuildRunnerContext context,
                                                   @NotNull final NuGetFetchParameters parameters,
                                                   @NotNull final NuGetVersionHolderImpl myVersion) {
    return new DelegatingBuildProcess(new DelegatingBuildProcess.Action() {
      @NotNull
      @Override
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
    });
  }


  private boolean requiresAuthentication(@NotNull NuGetFetchParameters fetch) {
    for (PackageSource src : fetch.getNuGetPackageSources()) {
      if (StringUtil.isEmptyOrSpaces(src.getUserName())) continue;
      if (StringUtil.isEmptyOrSpaces(src.getPassword())) continue;
      return true;
    }
    return false;
  }
}
