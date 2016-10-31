/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.agent.AgentRuntimeProperties.TEAMCITY_SERVER_URL;
import static jetbrains.buildServer.nuget.common.NuGetServerConstants.*;
import static jetbrains.buildServer.parameters.ReferencesResolverUtil.makeReference;
import static jetbrains.buildServer.web.util.WebUtil.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 17:52
 */
public class NuGetServerPropertiesProvider extends AbstractBuildParametersProvider {
  @NotNull private final NuGetServerSettings mySettings;
  @NotNull private final RootUrlHolder myRootUrlHolder;

  public NuGetServerPropertiesProvider(@NotNull final NuGetServerSettings settings, @NotNull final RootUrlHolder rootUrlHolder) {
    mySettings = settings;
    myRootUrlHolder = rootUrlHolder;
  }

  @NotNull
  @Override
  public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
    return getProperties();
  }

  @NotNull
  public Map<String, String> getProperties() {
    final Map<String, String> map = new HashMap<String, String>();
    if (mySettings.isNuGetServerEnabled()) {
      map.put(FEED_REFERENCE_AGENT_PROVIDED, makeReference(TEAMCITY_SERVER_URL) + combineContextPath(GUEST_AUTH_PREFIX, mySettings.getNuGetFeedControllerPathWithEndSlash()));
      map.put(FEED_AUTH_REFERENCE_AGENT_PROVIDED, makeReference(TEAMCITY_SERVER_URL) + combineContextPath(HTTP_AUTH_PREFIX, mySettings.getNuGetFeedControllerPathWithEndSlash()));
      map.put(Constants.SYSTEM_PREFIX + FEED_AUTH_REFERENCE_SERVER_PROVIDED, myRootUrlHolder.getRootUrl() + combineContextPath(HTTP_AUTH_PREFIX, mySettings.getNuGetFeedControllerPathWithEndSlash()));
    }
    return map;
  }
}
