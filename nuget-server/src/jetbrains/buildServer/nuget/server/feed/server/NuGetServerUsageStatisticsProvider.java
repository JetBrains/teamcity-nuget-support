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

import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.11.11 20:42
 */
public class NuGetServerUsageStatisticsProvider implements UsageStatisticsProvider, UsageStatisticsPresentationProvider {
  public static final String KEY = "jetbrains.nuget.server";
  private final NuGetServerRunnerSettings mySettings;

  public NuGetServerUsageStatisticsProvider(@NotNull final NuGetServerRunnerSettings settings) {
    mySettings = settings;
  }

  public void accept(@NotNull UsageStatisticsPublisher publisher) {
    if (mySettings.isNuGetFeedEnabled()) {
      publisher.publishStatistic(KEY, "enabled");
    }
  }

  public void accept(@NotNull UsageStatisticsPresentationManager presentationManager) {
    presentationManager.applyPresentation(KEY, "NuGet Feed Server", "NuGet", new UsageStatisticsFormatter() {
      @NotNull
      public String format(@Nullable Object statisticValue) {
        return statisticValue == null ? "disabled" : statisticValue.toString();
      }
    }, null);
  }
}
