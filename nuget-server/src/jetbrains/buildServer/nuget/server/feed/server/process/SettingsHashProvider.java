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

package jetbrains.buildServer.nuget.server.feed.server.process;

import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerTokens;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 10.11.11 10:30
 */
public class SettingsHashProvider {
  private final NuGetServerRunnerSettings mySettings;
  private final NuGetServerRunnerTokens myTokens;

  public SettingsHashProvider(@NotNull final NuGetServerRunnerSettings settings,
                              @NotNull final NuGetServerRunnerTokens tokens) {
    mySettings = settings;
    myTokens = tokens;
  }

  @NotNull
  public String getSettingsHash() {
    return "@" + mySettings.getLogFilePath() + "#" + mySettings.isNuGetFeedEnabled() + "#" + mySettings.getPackagesControllerUrl() + myTokens.getServerToken() + "!" + myTokens.getAccessToken();
  }
}
