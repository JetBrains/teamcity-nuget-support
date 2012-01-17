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

package jetbrains.buildServer.nuget.server.feed.server.controllers.requests;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.RecentEntriesCache;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.11.11 20:52
 */
public class RecentNuGetRequests {
  private static final Logger LOG = Logger.getInstance(RecentNuGetRequests.class.getName());
  
  private final RecentEntriesCache<String, String> myRequests = new RecentEntriesCache<String, String>(1000, false);
  
  public void reportFeedRequest(@NotNull final String url) {
    LOG.debug("NuGet Feed Request: " + url);
    myRequests.put(url, url);
  }
  
  @NotNull
  public Collection<String> getRecentRequests() {
    return new TreeSet<String>(myRequests.keySet());
  }

  public int getTotalRequests() {
    return myRequests.keySet().size();
  }
}
