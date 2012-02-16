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

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.buildTriggers.PolledBuildTrigger;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 14:01
 */
public class PackageCheckerSettingsImpl implements PackageCheckerSettings {

  public long getPackageCheckInterval() {
    return TeamCityProperties.getInteger("teamcity.nuget.packageCheckInterval", 5 * 60 * 1000);
  }

  public int getTriggerPollInterval() {
    return TeamCityProperties.getInteger("teamcity.nuget.trigger.pollInterval", PolledBuildTrigger.DEFAULT_POLL_TRIGGER_INTERVAL);
  }

  public long getPackageCheckRequestIdleRemoveInterval(long checkInterval) {
    //make sure package is not cleaned up before requests from trigger
    return Math.max(5 * checkInterval, 5 * getTriggerPollInterval());
  }

  public long getMinSleepInterval() {
    return getPackageCheckInterval() / 2;
  }

  public long getMaxSleepInterval() {
    return Math.min(3 * getPackageCheckInterval() / 2, getTriggerPollInterval() / 2);
  }

  public int getCheckerThreads() {
    return TeamCityProperties.getInteger("teamcity.nuget.trigger.pool", 4);
  }

  public boolean allowBulkMode(@NotNull PackageCheckRequest request) {
    return TeamCityProperties.getBooleanOrTrue("teamcity.nuget.trigger.bulkMode");
  }

  public int getMaxPackagesToQueryInBulk() {
    return TeamCityProperties.getInteger("teamcity.nuget.trigger.bulkMode.maxPackagesToQuery", 10);
  }
}
