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

package jetbrains.buildServer.nuget.agent.runner.publish.impl;

import jetbrains.buildServer.nuget.agent.runner.impl.NuGetRunnerStagesImpl;
import jetbrains.buildServer.nuget.agent.runner.publish.PublishStages;
import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 25.06.12 16:28
 */
public class PublishStagesImpl extends NuGetRunnerStagesImpl implements PublishStages {
  private final BuildProcessContinuation myLocateStage;
  private final BuildProcessContinuation myPublishStage;

  public PublishStagesImpl(@NotNull BuildProcessContinuation host) {
    super(host);
    myLocateStage = push();
    myPublishStage = push();
  }

  @NotNull
  public BuildProcessContinuation getLocateStage() {
    return myLocateStage;
  }

  @NotNull
  public BuildProcessContinuation getPublishStage() {
    return myPublishStage;
  }
}
