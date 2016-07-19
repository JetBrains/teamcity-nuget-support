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

package jetbrains.buildServer.nuget.feed.server.controllers.requests;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.ItemProcessor;
import jetbrains.buildServer.util.RecentEntriesCache;
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
  private final RecentEntriesCache<String, Long> myFeedRequests = new RecentEntriesCache<String, Long>(5000, false);
  private final RecentEntriesCache<Long, Long> myFeedRequestTimes = new RecentEntriesCache<Long, Long>(20000, false);

  public synchronized void reportFeedRequest(@NotNull final String url) {
    LOG.debug("NuGet Feed request processing started for " + url);
    final long time = getTime();
    myFeedRequestTimes.put(time, time);
    myFeedRequests.put(url, time);
  }

  public synchronized void reportFeedRequestFinished(@NotNull final String url, long requestProcessingDuration) {
    LOG.debug("NuGet Feed Request request processing finsihed in " + requestProcessingDuration + "ms for " + url);
    final Long requestProcessingStartTime = myFeedRequests.get(url);
    if(requestProcessingStartTime != null) {
      myFeedRequestTimes.put(requestProcessingStartTime, requestProcessingDuration);
    }
  }

  public synchronized int getTotalRequests() {
    removeOldRequests();
    return myFeedRequestTimes.size();
  }

  public synchronized long getAverageResponseTime(){
    removeOldRequests();
    int numberOfProcessedRequests = myFeedRequestTimes.size();
    if(numberOfProcessedRequests == 0) return 0;

    final Long[] overalDuration = {0L};
    myFeedRequestTimes.forEach(new ItemProcessor<Long>() {
      @Override
      public boolean processItem(Long item) {
        overalDuration[0] += item;
        return true;
      }
    });
    return overalDuration[0] / numberOfProcessedRequests;
  }

  @NotNull
  Collection<String> getRecentRequests() {
    return new TreeSet<String>(myFeedRequests.keySet());
  }

  private void removeOldRequests() {
    final long minStartTime = getTime() - Dates.ONE_DAY;
    for (Long requestProcessingStartTime : myFeedRequestTimes.keySet()){
      if(requestProcessingStartTime < minStartTime){
        myFeedRequestTimes.remove(requestProcessingStartTime);
      }
    }
  }

  private long getTime() {
    return new Date().getTime();
  }
}
