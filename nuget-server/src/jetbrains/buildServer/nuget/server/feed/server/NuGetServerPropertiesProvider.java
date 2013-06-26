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

package jetbrains.buildServer.nuget.server.feed.server;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.agent.AgentRuntimeProperties.TEAMCITY_SERVER_URL;
import static jetbrains.buildServer.nuget.common.NuGetServerConstants.FEED_AUTH_REFERENCE;
import static jetbrains.buildServer.nuget.common.NuGetServerConstants.FEED_REFERENCE;
import static jetbrains.buildServer.parameters.ReferencesResolverUtil.makeReference;
import static jetbrains.buildServer.web.util.WebUtil.GUEST_AUTH_PREFIX;
import static jetbrains.buildServer.web.util.WebUtil.HTTP_AUTH_PREFIX;
import static jetbrains.buildServer.web.util.WebUtil.combineContextPath;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 17:52
 */
public class NuGetServerPropertiesProvider extends AbstractBuildParametersProvider {
  @NotNull private final NuGetServerSettings mySettings;

  public NuGetServerPropertiesProvider(@NotNull final NuGetServerSettings settings) {
    mySettings = settings;
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
      map.put(FEED_REFERENCE, makeReference(TEAMCITY_SERVER_URL) + combineContextPath(GUEST_AUTH_PREFIX, mySettings.getNuGetFeedControllerPath()));
      map.put(FEED_AUTH_REFERENCE, makeReference(TEAMCITY_SERVER_URL) + combineContextPath(HTTP_AUTH_PREFIX, mySettings.getNuGetFeedControllerPath()));
    }
    return map;
  }
}
