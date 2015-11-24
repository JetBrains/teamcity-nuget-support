/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.toolRegistry.impl.impl;

import jetbrains.buildServer.nuget.server.toolRegistry.FetchAvailableToolsResult;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolsPolicy;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.AvailableToolsState;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.08.11 16:33
 */
public class AvailableToolsStateImpl implements AvailableToolsState {
  private static final long TIMEOUT = 1000 * 60 * 15; //15 min

  @NotNull private final TimeService myTime;
  @NotNull private final Collection<AvailableToolsFetcher> myFetchers;

  private FetchAvailableToolsResult myFetchResult;
  private long lastRequest = 0;

  public AvailableToolsStateImpl(@NotNull final TimeService time, @NotNull Collection<AvailableToolsFetcher> fetchers) {
    myTime = time;
    myFetchers = fetchers;
  }

  @Nullable
  public DownloadableNuGetTool findTool(@NotNull final String id) {
    final FetchAvailableToolsResult fetchResult = myFetchResult;
    if (fetchResult != null) {
      for (DownloadableNuGetTool tool : fetchResult.getFetchedTools()) {
        if(tool.getId().equals(id)) {
          return tool;
        }
      }
    }
    return null;
  }

  @NotNull
  public FetchAvailableToolsResult getAvailable(ToolsPolicy policy) {
    FetchAvailableToolsResult fetchResult = myFetchResult;
    if (policy == ToolsPolicy.FetchNew
            || fetchResult == null
            || lastRequest + TIMEOUT < myTime.now()) {
      myFetchResult = null;
      myFetchResult = fetchResult = fetchAvailable();
      lastRequest = myTime.now();
    }
    return fetchResult;
  }

  private FetchAvailableToolsResult fetchAvailable() {
    return FetchAvailableToolsResult.join(CollectionsUtil.convertCollection(myFetchers, new Converter<FetchAvailableToolsResult, AvailableToolsFetcher>() {
      public FetchAvailableToolsResult createFrom(@NotNull AvailableToolsFetcher fetcher) {
        return fetcher.fetchAvailable();
      }
    }));
  }
}
