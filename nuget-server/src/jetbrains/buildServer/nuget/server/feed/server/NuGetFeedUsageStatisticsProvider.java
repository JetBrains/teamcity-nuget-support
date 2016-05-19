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
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.impl.providers.BaseDefaultUsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedUsageStatisticsProvider extends BaseDefaultUsageStatisticsProvider {

  public static final String GROUP_NAME = "NuGet Feed Usage";

  private static final String TOTAL_REQUESTS_STAT_ID = "jetbrains.nuget.feedDailyRequests";
  private static final String AVG_RESPONSE_TIME_STAT_ID = "jetbrains.nuget.avgResponseTime";
  private static final String TOTAL_PACKAGES_STAT_ID = "jetbrains.nuget.totalPackages";
  private static final String DIFF_PACKAGES_STAT_ID = "jetbrains.nuget.packagesIds";

  private static final PositionAware NUGET_API_CALLS_GROUP = new PositionAware() {
    @NotNull
    public String getOrderId() {
      return "nuget_api_calls";
    }

    @NotNull
    public PositionConstraint getConstraint() {
      return PositionConstraint.UNDEFINED;
    }
  };

  private final RecentNuGetRequests myRequests;
  private final PackagesIndex myIndex;
  private final NuGetServerSettings mySettings;

  public NuGetFeedUsageStatisticsProvider(@NotNull final RecentNuGetRequests requests,
                                          @NotNull final NuGetServerSettings settings,
                                          @NotNull final PackagesIndex index) {
    myRequests = requests;
    mySettings = settings;
    myIndex = index;
    myGroupName = GROUP_NAME;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return NUGET_API_CALLS_GROUP;
  }

  @Override
  protected void accept(@NotNull UsageStatisticsPublisher publisher, @NotNull UsageStatisticsPresentationManager presentationManager) {
    if (mySettings.isNuGetServerEnabled()) {
      publisher.publishStatistic(TOTAL_REQUESTS_STAT_ID, myRequests.getTotalRequests());
      presentationManager.applyPresentation(TOTAL_REQUESTS_STAT_ID, "Feed Requests Count per Day", myGroupName, null, null);

      publisher.publishStatistic(AVG_RESPONSE_TIME_STAT_ID, myRequests.getAverageResponseTime());
      presentationManager.applyPresentation(AVG_RESPONSE_TIME_STAT_ID, "Average Feed Response Time (ms)", myGroupName, null, null);

      final Pair<Integer, Integer> indexEntriesCount = countIndexEntries();
      publisher.publishStatistic(TOTAL_PACKAGES_STAT_ID, indexEntriesCount.first);
      presentationManager.applyPresentation(TOTAL_PACKAGES_STAT_ID, "Packages Count", myGroupName, null, null);
      publisher.publishStatistic(DIFF_PACKAGES_STAT_ID, indexEntriesCount.second);
      presentationManager.applyPresentation(DIFF_PACKAGES_STAT_ID, "Different Package Ids Count", myGroupName, null, null);
    }
  }

  private Pair<Integer, Integer> countIndexEntries() {
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
}
