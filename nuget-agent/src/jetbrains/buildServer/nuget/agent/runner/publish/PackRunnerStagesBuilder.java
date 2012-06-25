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

package jetbrains.buildServer.nuget.agent.runner.publish;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.runner.impl.AuthStagesBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 25.06.12 16:46
 */
public class PackRunnerStagesBuilder {
  private final AuthStagesBuilder myAuthBuilder;
  private final NuGetActionFactory myActionFactory;

  public PackRunnerStagesBuilder(@NotNull final AuthStagesBuilder authBuilder,
                                 @NotNull final NuGetActionFactory actionFactory) {
    myAuthBuilder = authBuilder;
    myActionFactory = actionFactory;
  }

  public void createStages(@NotNull final BuildRunnerContext context,
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
}
