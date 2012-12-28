/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.controllers.requests;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.RecentEntriesCache;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.11.11 20:52
 */
public class RecentNuGetRequests {
  private static final Logger LOG = Logger.getInstance(RecentNuGetRequests.class.getName());
  private final RecentEntriesCache<String, String> myRequests = new RecentEntriesCache<String, String>(5000, false);
  private final RecentEntriesCache<Long, Long> myRequestTimes = new RecentEntriesCache<Long, Long>(20000, false);
  
  public void reportFeedRequest(@NotNull final String url) {
    LOG.debug("NuGet Feed started for " + url);

    final long time = getTime();
    myRequestTimes.put(time, time);
    //TODO: trim identifiers in request to merge same trees
    myRequests.put(url, url);
  }

  private long getTime() {
    return new Date().getTime();
  }

  public void reportFeedRequestFinished(@NotNull final String url, long time) {
    LOG.debug("NuGet Feed Request finsihed in " + time + "ms for " + url);
  }
  
  @NotNull
  public Collection<String> getRecentRequests() {
    return new TreeSet<String>(myRequests.keySet());
  }

  public int getTotalRequests() {
    final long SPAN = Dates.ONE_DAY;

    final long now = getTime();
    final long startTime = now - SPAN;

    myRequestTimes.removeValues(new Filter<Long>() {
      public boolean accept(@NotNull Long data) {
        return data < startTime;
      }
    });
    return myRequestTimes.size();
  }
}
