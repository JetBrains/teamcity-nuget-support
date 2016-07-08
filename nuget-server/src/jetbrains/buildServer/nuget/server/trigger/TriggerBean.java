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

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.nuget.common.FeedConstants;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.07.11 14:56
 */
public class TriggerBean {
  public String getNuGetToolTypeKey() {  return FeedConstants.NUGET_COMMANDLINE; }
  public String getNuGetExeKey() {  return TriggerConstants.NUGET_PATH_PARAM_NAME; }
  public String getSourceKey() {  return TriggerConstants.SOURCE;   }
  public String getPackageKey() {  return TriggerConstants.PACKAGE;   }
  public String getVersionKey() {  return TriggerConstants.VERSION;   }
  public String getPrereleaseKey() {  return TriggerConstants.INCLUDE_PRERELEASE;   }
  public String getUsername() { return TriggerConstants.USERNAME; }
  public String getPassword() { return TriggerConstants.PASSWORD; }
}
