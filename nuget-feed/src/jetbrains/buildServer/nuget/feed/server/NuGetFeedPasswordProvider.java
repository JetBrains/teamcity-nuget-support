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

package jetbrains.buildServer.nuget.feed.server;

import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.build.steps.BuildStepsEditor;
import jetbrains.buildServer.serverSide.build.steps.StepContext;
import jetbrains.buildServer.serverSide.impl.SBuildStepsCollection;
import jetbrains.buildServer.serverSide.impl.build.steps.BuildStartContextBase;
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Hides tokens for built-in NuGet feed.
 */
public class NuGetFeedPasswordProvider implements PasswordsProvider {

  @NotNull
  @Override
  public Collection<Parameter> getPasswordParameters(@NotNull SBuild build) {
    final List<Parameter> passwords = new ArrayList<>();
    final String feedApiKey = build.getParametersProvider().get(NuGetServerConstants.FEED_REFERENCE_AGENT_API_KEY_PROVIDED);

    if (!StringUtil.isEmptyOrSpaces(feedApiKey)) {
      passwords.add(new SimpleParameter(NuGetServerConstants.FEED_REFERENCE_AGENT_API_KEY_PROVIDED, feedApiKey));
    }

    final BuildPromotionEx buildPromotion = (BuildPromotionEx)build.getBuildPromotion();
    final SBuildStepsCollection runners = buildPromotion.getBuildSettings().getAllBuildRunners();
    if (!(runners instanceof BuildStartContextBase)) return passwords;

    final BuildStepsEditor steps = ((BuildStartContextBase)runners).getRootSteps();
    for (StepContext step : steps.getSteps()) {
      if (!PackagesConstants.PUBLISH_RUN_TYPE.equals(step.getRunType().getType())) continue;

      final String apiKey = step.getParameters().get(PackagesConstants.NUGET_API_KEY);
      if (StringUtil.isEmptyOrSpaces(apiKey)) continue;

      passwords.add(new SimpleParameter(PackagesConstants.NUGET_API_KEY, apiKey));
    }

    return passwords;
  }
}
