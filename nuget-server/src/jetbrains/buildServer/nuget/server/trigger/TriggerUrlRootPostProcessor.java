/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.nuget.server.TriggerUrlPostProcessor;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.agent.AgentRuntimeProperties.TEAMCITY_SERVER_URL;

/**
 * Created 26.06.13 19:02
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class TriggerUrlRootPostProcessor implements TriggerUrlPostProcessor, PositionAware {
  private final RootUrlHolder myHolder;

  public TriggerUrlRootPostProcessor(@NotNull RootUrlHolder holder) {
    myHolder = holder;
  }

  @NotNull
  public String updateTriggerUrl(@NotNull SBuildType buildType, @NotNull String source) {
    if (!ReferencesResolverUtil.mayContainReference(source)) return source;
    return source.replace(ReferencesResolverUtil.makeReference(TEAMCITY_SERVER_URL), myHolder.getRootUrl());
  }

  @NotNull
  @Override
  public String getOrderId() {
    return TriggerUrlRootPostProcessor.class.getName();
  }

  @NotNull
  @Override
  public PositionConstraint getConstraint() {
    return PositionConstraint.last();
  }
}
