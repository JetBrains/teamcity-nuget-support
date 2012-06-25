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

package jetbrains.buildServer.nuget.agent.runner.publish;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.runner.NuGetRunnerBase;
import jetbrains.buildServer.nuget.agent.runner.impl.AuthStagesBuilder;
import jetbrains.buildServer.nuget.agent.runner.publish.impl.PackStagesImpl;
import jetbrains.buildServer.nuget.agent.util.CompositeBuildProcess;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 15:15
 */
public class PackagesPublishRunner extends NuGetRunnerBase {
  private final AuthStagesBuilder myAuthBuilder;

  public PackagesPublishRunner(@NotNull final AuthStagesBuilder authBuilder,
                               @NotNull final NuGetActionFactory actionFactory,
                               @NotNull final PackagesParametersFactory parametersFactory) {
    super(actionFactory, parametersFactory);
    myAuthBuilder = authBuilder;
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    final NuGetPublishParameters params = myParametersFactory.loadPublishParameters(context);

    final CompositeBuildProcess process = new CompositeBuildProcessImpl();
    final PackStages stages = new PackStagesImpl(process);

    createStages(context, stages, params);
    return process;
  }

  private void createStages(@NotNull final BuildRunnerContext context,
                            @NotNull final PackStages stages,
                            @NotNull final NuGetPublishParameters params) throws RunBuildException {
    myAuthBuilder.buildStages(stages, context, params);

    stages.getLocateStage().pushBuildProcess(new MatchFilesBuildProcess(context, params, new MatchFilesBuildProcess.Callback() {
      public void fileFound(@NotNull File file) throws RunBuildException {
        stages.getPublishStage().pushBuildProcess(
                myActionFactory.createPush(context, params, file)
        );
      }
    }));
  }

  @NotNull
  public String getType() {
    return PackagesConstants.PUBLISH_RUN_TYPE;
  }
}