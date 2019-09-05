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

package jetbrains.buildServer.nuget.common.auth;

/**
 * Created by Evgeniy.Koshkin on 17.12.2015
 */
public class NuGetAuthConstants {
  public static final String TEAMCITY_NUGET_FEEDS_ENV_VAR = "TEAMCITY_NUGET_FEEDS";
  public static final String NUGET_CREDENTIALPROVIDERS_PATH_ENV_VAR = "NUGET_CREDENTIALPROVIDERS_PATH";
  public static final String NUGET_PLUGIN_PATH_ENV_VAR = "NUGET_PLUGIN_PATHS";
  public static final String NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS_ENV_VAR = "NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS";
  public static final String NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS_ENV_VAR = "NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS";

  public static final int NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS = 30;
  public static final int NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS = 30;
}
