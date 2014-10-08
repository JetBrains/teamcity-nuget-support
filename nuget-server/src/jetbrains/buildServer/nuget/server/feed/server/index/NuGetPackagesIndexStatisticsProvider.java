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

package jetbrains.buildServer.nuget.server.feed.server.index;

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerSettings;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.11.11 20:42
 */
public class NuGetPackagesIndexStatisticsProvider implements UsageStatisticsProvider, UsageStatisticsPresentationProvider {
  public static final String TOTAL_PACKAGES = "jetbrains.nuget.totalPackages";
  public static final String DIFF_PACKAGES = "jetbrains.nuget.packagesIds";
  public static final String NUGET_SERVER_STAT_GROUP_NAME = "NuGet Packages Index";

  private final NuGetServerSettings mySettings;
  private final PackagesIndex myIndex;

  public NuGetPackagesIndexStatisticsProvider(@NotNull final NuGetServerSettings settings,
                                              @NotNull final PackagesIndex index) {
    mySettings = settings;
    myIndex = index;
  }

  public void accept(@NotNull UsageStatisticsPublisher publisher) {
    if (mySettings.isNuGetServerEnabled()) {
      final Pair<Integer, Integer> entries = countEntries();
      publisher.publishStatistic(TOTAL_PACKAGES, entries.first);
      publisher.publishStatistic(DIFF_PACKAGES, entries.second);
    }
  }

  public void accept(@NotNull UsageStatisticsPresentationManager presentationManager) {
    presentationManager.applyPresentation(TOTAL_PACKAGES, "Packages Count", NUGET_SERVER_STAT_GROUP_NAME, null, null);
    presentationManager.applyPresentation(DIFF_PACKAGES, "Different Package Ids Count", NUGET_SERVER_STAT_GROUP_NAME, null, null);
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
}
