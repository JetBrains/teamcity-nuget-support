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

package jetbrains.buildServer.nuget.feed.server.impl;

import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.nuget.feed.server.impl.UrlUtil.join;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 17.01.12 17:14
 */
public class NuGetServerSettingsImpl implements NuGetServerSettings {
  public static final String PATH = "/app/nuget/v1/FeedService.svc";
  public static final String PATH_END_SLASH = "/app/nuget/v1/FeedService.svc/";
  private final NuGetServerFeedSettingsImpl myDotNetSettings;

  public NuGetServerSettingsImpl(@NotNull final NuGetServerFeedSettingsImpl dotNetSettings) {
    myDotNetSettings = dotNetSettings;
  }

  public boolean isNuGetServerEnabled() {
    return myDotNetSettings.isNuGetJavaFeedEnabled();
  }

  public boolean isFilteringByTargetFrameworkEnabled() {
    return myDotNetSettings.isFilteringByTargetFrameworkEnabled();
  }

  @NotNull
  public String getNuGetFeedControllerPath() {
    //NOTE: Do not change it unless you want to break compatibility
    return PATH;
  }

  @Override
  public String getNuGetFeedControllerPathWithEndSlash() {
    return PATH_END_SLASH;
  }

  @NotNull
  public String getNuGetHttpAuthFeedControllerPath() {
    return join(WebUtil.HTTP_AUTH_PREFIX, PATH_END_SLASH);
  }

  @NotNull
  public String getNuGetGuestAuthFeedControllerPath() {
    return join(WebUtil.GUEST_AUTH_PREFIX, PATH_END_SLASH);
  }
}
