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

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider;
import jetbrains.buildServer.serverSide.parameters.ParameterDescriptionProvider;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 17:52
 */
public class NuGetServerPropertiesProvider implements BuildParametersProvider, ParameterDescriptionProvider {
  @NotNull private final NuGetServerSettings mySettings;

  public NuGetServerPropertiesProvider(@NotNull final NuGetServerSettings settings) {
    mySettings = settings;
  }

  @NotNull
  public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
    Map<String, String> map = new HashMap<String, String>();
    if (mySettings.isNuGetServerEnabled()) {
      //TODO: set here httpAuth url as NuGet is able to support authentication.
      map.put(NuGetServerConstants.FEED_REFERENCE, "%teamcity.serverUrl%" + WebUtil.combineContextPath(WebUtil.GUEST_AUTH_PREFIX, mySettings.getNuGetFeedControllerPath()));
    }
    return map;
  }

  @NotNull
  public Collection<String> getParametersAvailableOnAgent(@NotNull SBuild build) {
    return Collections.emptyList();
  }

  public String describe(@NotNull String paramName) {
    if (NuGetServerConstants.FEED_REFERENCE.equals(paramName)) return "Contains URL to TeamCity provided NuGet feed";
    return null;
  }
}
