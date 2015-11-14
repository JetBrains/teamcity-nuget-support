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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.toolRegistry.FetchException;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolsPolicy;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.AvailableToolsState;
import jetbrains.buildServer.nuget.server.util.SemanticVersion;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.08.11 16:33
 */
public class AvailableToolsStateImpl implements AvailableToolsState {
  private static final Logger LOG = Logger.getInstance(AvailableToolsStateImpl.class.getName());
  private static final long TIMEOUT = 1000 * 60 * 15; //15 min
  private static final Comparator<? super NuGetTool> COMPARATOR = new Comparator<NuGetTool>() {
    public int compare(NuGetTool o1, NuGetTool o2) {
      return SemanticVersion.compareAsVersions(o2.getVersion(), o1.getVersion());
    }
  };

  @NotNull private final TimeService myTime;
  @NotNull private final Collection<AvailableToolsFetcher> myFetchers;

  private Set<DownloadableNuGetTool> myTools;
  private long lastRequest = 0;

  public AvailableToolsStateImpl(@NotNull final TimeService time, @NotNull Collection<AvailableToolsFetcher> fetchers) {
    myTime = time;
    myFetchers = fetchers;
  }

  @Nullable
  public DownloadableNuGetTool findTool(@NotNull final String id) {
    final Set<DownloadableNuGetTool> tools = myTools;
    if (tools != null) {
      for (DownloadableNuGetTool tool : tools) {
        if(tool.getId().equals(id)) {
          return tool;
        }
      }
    }
    return null;
  }

  @NotNull
  public Set<DownloadableNuGetTool> getAvailable(ToolsPolicy policy) throws FetchException {
    Set<DownloadableNuGetTool> nuGetTools = myTools;
    if (policy == ToolsPolicy.FetchNew
            || nuGetTools == null
            || lastRequest + TIMEOUT < myTime.now()) {
      myTools = null;
      myTools = nuGetTools = fetchAvailable();
      lastRequest = myTime.now();
    }
    return nuGetTools;
  }

  private Set<DownloadableNuGetTool> fetchAvailable() {
    final TreeSet<DownloadableNuGetTool> available = new TreeSet<DownloadableNuGetTool>(COMPARATOR);
    for(AvailableToolsFetcher fetcher : myFetchers){
      try {
        available.addAll(fetcher.fetchAvailable());
      } catch (FetchException e) {
        LOG.warn("Failed fetch available NuGet tools from " + fetcher.getSourceDisplayName(), e);
      }
    }
    return Collections.unmodifiableSet(available);
  }
}
