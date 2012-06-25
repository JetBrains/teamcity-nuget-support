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

package jetbrains.buildServer.nuget.agent.runner.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetVersionHolder;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetVersionHolderImpl;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFeedParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 25.06.12 16:06
 */
public class AuthStagesBuilder {
  private final NuGetActionFactory myActionFactory;

  public AuthStagesBuilder(@NotNull NuGetActionFactory actionFactory) {
    myActionFactory = actionFactory;
  }

  @NotNull
  public NuGetVersionHolder buildStages(@NotNull final NuGetRunnerStages stages,
                                        @NotNull final BuildRunnerContext context,
                                        @NotNull final NuGetFeedParameters parameters) throws RunBuildException {

    final NuGetVersionHolderImpl myVersion = new NuGetVersionHolderImpl();

    stages.getCheckVersionStage().pushBuildProcess(
            myActionFactory.createVersionCheckCommand(
                    context,
                    myVersion,
                    parameters));

    if (requiresAuthentication(parameters)) {
      stages.getAuthenticateStage().pushBuildProcess(createAuthProcess(context, parameters, myVersion));
    }

    return myVersion;
  }

  private boolean requiresAuthentication(@NotNull NuGetFeedParameters fetch) throws RunBuildException {
    for (PackageSource src : fetch.getNuGetPackageSources()) {
      if (StringUtil.isEmptyOrSpaces(src.getUserName())) continue;
      if (StringUtil.isEmptyOrSpaces(src.getPassword())) continue;
      return true;
    }
    return false;
  }

  private DelegatingBuildProcess createAuthProcess(@NotNull final BuildRunnerContext context,
                                                   @NotNull final NuGetFeedParameters parameters,
                                                   @NotNull final NuGetVersionHolder version) {
    return new DelegatingBuildProcess(new DelegatingBuildProcess.Action() {
      @NotNull
      @Override
      public BuildProcess startImpl() throws RunBuildException {
        if (version.getNuGetVerion().supportAuth()) {
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

}
