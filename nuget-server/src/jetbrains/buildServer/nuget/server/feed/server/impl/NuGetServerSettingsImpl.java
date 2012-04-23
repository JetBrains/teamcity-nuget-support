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

package jetbrains.buildServer.nuget.server.feed.server.impl;

import jetbrains.buildServer.nuget.server.feed.server.NuGetServerSettings;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.nuget.server.feed.server.impl.UrlUtil.join;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 17.01.12 17:14
 */
public class NuGetServerSettingsImpl implements NuGetServerSettings {
  public static final String PATH = "/app/nuget/v1/FeedService.svc";
  private final NuGetServerFeedSettingsImpl myDotNetSettings;

  public NuGetServerSettingsImpl(@NotNull final NuGetServerFeedSettingsImpl dotNetSettings) {
    myDotNetSettings = dotNetSettings;
  }

  public boolean isNuGetServerEnabled() {
    return myDotNetSettings.isNuGetJavaFeedEnabled();
  }

  @NotNull
  public String getNuGetFeedControllerPath() {
    //NOTE: Do not change it unless you want to break compatibility
    return PATH;
  }

  @NotNull
  public String getNuGetHttpAuthFeedControllerPath() {
    return join(WebUtil.HTTP_AUTH_PREFIX, getNuGetFeedControllerPath());
  }

  @NotNull
  public String getNuGetGuestAuthFeedControllerPath() {
    return join(WebUtil.GUEST_AUTH_PREFIX, getNuGetFeedControllerPath());
  }
}
