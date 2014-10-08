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

import jetbrains.buildServer.nuget.server.feed.server.controllers.requests.RecentNuGetRequests;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetPackagesIndexStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.impl.providers.BaseDefaultUsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedUsageStatisticsProvider extends BaseDefaultUsageStatisticsProvider {

  private static final String FEED_ENABLED_KEY = "jetbrains.nuget.server";
  private static final String FUNCTIONS_STAT_ID_FORMAT = "jetbrains.nuget.api.functions.[%s]";
  private static final String TOTAL_REQUESTS_STAT_ID = "jetbrains.nuget.feedDailyRequests";
  private static final String METADATA_REQUESTS_STAT_ID = "jetbrains.nuget.feedDailyRequests.metadata";

  private static final PositionAware NUGET_API_CALLS_GROUP = new PositionAware() {
    @NotNull
    public String getOrderId() {
      return "nuget_api_calls";
    }

    @NotNull
    public PositionConstraint getConstraint() {
      return PositionConstraint.after(NuGetPackagesIndexStatisticsProvider.NUGET_SERVER_STAT_GROUP_NAME);
    }
  };

  private final RecentNuGetRequests myRequests;
  private final NuGetServerSettings mySettings;

  public NuGetFeedUsageStatisticsProvider(@NotNull final RecentNuGetRequests requests, @NotNull final NuGetServerSettings settings) {
    myRequests = requests;
    mySettings = settings;
    myGroupName = "NuGet Feed Usage";
    setIdFormat(FUNCTIONS_STAT_ID_FORMAT);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return NUGET_API_CALLS_GROUP;
  }

  @Override
  protected void accept(@NotNull UsageStatisticsPublisher publisher, @NotNull UsageStatisticsPresentationManager presentationManager) {
    if (mySettings.isNuGetServerEnabled()) {

      publisher.publishStatistic(FEED_ENABLED_KEY, "enabled");
      presentationManager.applyPresentation(FEED_ENABLED_KEY, "NuGet Feed", myGroupName, new UsageStatisticsFormatter() {
        @NotNull
        public String format(@Nullable Object statisticValue) {
          return statisticValue == null ? "disabled" : statisticValue.toString();
        }
      }, null);

      final int totalRequests = myRequests.getTotalRequests();

      publisher.publishStatistic(TOTAL_REQUESTS_STAT_ID, totalRequests);
      presentationManager.applyPresentation(TOTAL_REQUESTS_STAT_ID, "Feed Requests Count per Day", myGroupName, null, null);

      final UsageStatisticsFormatter formatter = new PercentageFormatter(totalRequests);

      publisher.publishStatistic(METADATA_REQUESTS_STAT_ID, myRequests.getMetadataRequestsCount());
      presentationManager.applyPresentation(METADATA_REQUESTS_STAT_ID, "Feed Metadata Requests Count per Day", myGroupName, formatter, null);

      final Map<String, Integer> functionCalls = myRequests.getFunctionCalls();
      for(String functionName : functionCalls.keySet()){
        final String statisticId = makeId(functionName);
        presentationManager.applyPresentation(statisticId, String.format("%s Function Calls per Day", functionName), myGroupName, formatter, null);
        publisher.publishStatistic(statisticId, functionCalls.get(functionName));
      }
    }
  }
}
