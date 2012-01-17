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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 31.10.11 12:09
 */
public interface NuGetServerDotNetSettingsEx extends NuGetServerDotNetSettings {
  void setNuGetDotNetFeedEnabled(boolean newValue);

  void setTeamCityBaseUrl(@NotNull String url);
  void setDefaultTeamCityBaseUrl();

  /**
   * @return custom-set TeamCity url or null
   */
  @Nullable
  String getCustomTeamCityBaseUrl();
}
