/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed;

import jetbrains.buildServer.serverSide.TeamCityProperties;
import org.jetbrains.annotations.NotNull;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetAPIVersion {
  public static final String TEAMCITY_NUGET_API_VERSION_PROP_NAME = "teamcity.nuget.api.version";
  public static final String V1 = "v1";
  public static final String V2 = "v2";

  public static boolean shouldUseV2() {
    return getVersionToUse().equalsIgnoreCase(V2);
  }

  @NotNull
  public static String getVersionToUse() {
    return TeamCityProperties.getProperty(TEAMCITY_NUGET_API_VERSION_PROP_NAME, V1);
  }
}
