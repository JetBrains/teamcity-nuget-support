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

package jetbrains.buildServer.nuget.server.feed.server;

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.nuget.server.feed.server.controllers.requests.RecentNuGetRequests;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.11.11 20:42
 */
public class NuGetServerUsageStatisticsProvider implements UsageStatisticsProvider, UsageStatisticsPresentationProvider {
  public static final String SERVER_ENABLED_KEY = "jetbrains.nuget.server";
  public static final String TOTAL_REQUESTS = "jetbrains.nuget.feedDailyRequests";
  public static final String TOTAL_PACKAGES = "jetbrains.nuget.totalPackages";
  public static final String DIFF_PACKAGES = "jetbrains.nuget.packagesIds";
  private final NuGetServerSettings mySettings;
  private final RecentNuGetRequests myRequests;
  private final PackagesIndex myIndex;

  public NuGetServerUsageStatisticsProvider(@NotNull final NuGetServerSettings settings,
                                            @NotNull final RecentNuGetRequests requests,
                                            @NotNull final PackagesIndex index) {
    mySettings = settings;
    myRequests = requests;
    myIndex = index;
  }

  public void accept(@NotNull UsageStatisticsPublisher publisher) {
    if (mySettings.isNuGetServerEnabled()) {
      publisher.publishStatistic(SERVER_ENABLED_KEY, "enabled");
      publisher.publishStatistic(TOTAL_REQUESTS, myRequests.getTotalRequests());
      final Pair<Integer, Integer> entries = countEntries();
      publisher.publishStatistic(TOTAL_PACKAGES, entries.first);
      publisher.publishStatistic(DIFF_PACKAGES, entries.second);
    }
  }
  
  private Pair<Integer, Integer> countEntries() {
    int count = 0;
    final Set<String> packagesCounter = new HashSet<String>();
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();
    while(it.hasNext()) {
      NuGetIndexEntry next = it.next();
      packagesCounter.add(next.getAttributes().get("Id"));
      count++;
    }
    return Pair.create(count, packagesCounter.size());
  }

  public void accept(@NotNull UsageStatisticsPresentationManager presentationManager) {
    presentationManager.applyPresentation(TOTAL_REQUESTS, "Feed Requests Count per Day", "NuGet", null, null);
    presentationManager.applyPresentation(TOTAL_PACKAGES, "Packages Count", "NuGet", null, null);
    presentationManager.applyPresentation(DIFF_PACKAGES, "Different Package Ids Count", "NuGet", null, null);
    presentationManager.applyPresentation(SERVER_ENABLED_KEY, "NuGet Feed Server", "NuGet", new UsageStatisticsFormatter() {
      @NotNull
      public String format(@Nullable Object statisticValue) {
        return statisticValue == null ? "disabled" : statisticValue.toString();
      }
    }, null);
  }
}
