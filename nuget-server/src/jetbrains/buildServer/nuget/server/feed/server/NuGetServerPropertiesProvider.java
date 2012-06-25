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

package jetbrains.buildServer.nuget.server.feed.server;

import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.agent.AgentRuntimeProperties.TEAMCITY_SERVER_URL;
import static jetbrains.buildServer.nuget.server.feed.server.NuGetServerConstants.AUTH_FEED_REFERENCE;
import static jetbrains.buildServer.nuget.server.feed.server.NuGetServerConstants.FEED_REFERENCE;
import static jetbrains.buildServer.web.util.WebUtil.*;

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
  public Map<String, String> getParameters(@NotNull final SBuild build, final boolean emulationMode) {
    final Map<String, String> map = new HashMap<String, String>();
    if (mySettings.isNuGetServerEnabled()) {
      final String baseUrl = ReferencesResolverUtil.makeReference(TEAMCITY_SERVER_URL);
      map.put(FEED_REFERENCE, baseUrl + combineContextPath(GUEST_AUTH_PREFIX, mySettings.getNuGetFeedControllerPath()));
      map.put(AUTH_FEED_REFERENCE, baseUrl + combineContextPath(HTTP_AUTH_PREFIX, mySettings.getNuGetFeedControllerPath()));
    }
    return map;
  }
}
