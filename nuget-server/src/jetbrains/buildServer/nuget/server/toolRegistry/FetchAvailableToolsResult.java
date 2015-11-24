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

package jetbrains.buildServer.nuget.server.toolRegistry;

import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.DownloadableNuGetTool;
import jetbrains.buildServer.nuget.server.util.SemanticVersion;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Evgeniy.Koshkin
 */
public class FetchAvailableToolsResult {

  private static final Comparator<? super NuGetTool> COMPARATOR = new Comparator<NuGetTool>() {
    public int compare(NuGetTool o1, NuGetTool o2) {
      return SemanticVersion.compareAsVersions(o2.getVersion(), o1.getVersion());
    }
  };

  @NotNull private Set<DownloadableNuGetTool> myFetchedTools;
  @NotNull private Collection<String> myErrors;

  public FetchAvailableToolsResult(@NotNull Set<DownloadableNuGetTool> fetchedTools, @NotNull Collection<String> errors) {
    myFetchedTools = fetchedTools;
    myErrors = errors;
  }

  @NotNull
  public static FetchAvailableToolsResult createSuccessfull(@NotNull Collection<DownloadableNuGetTool> fetchedTools) {
    final TreeSet<DownloadableNuGetTool> tools = new TreeSet<DownloadableNuGetTool>(COMPARATOR);
    tools.addAll(fetchedTools);
    return new FetchAvailableToolsResult(Collections.unmodifiableSet(tools), Collections.<String>emptyList());
  }

  @NotNull
  public static FetchAvailableToolsResult createError(@NotNull String error){
    return new FetchAvailableToolsResult(Collections.<DownloadableNuGetTool>emptySet(), Collections.singleton(error));
  }

  @NotNull
  public static FetchAvailableToolsResult createError(@NotNull String error, @NotNull Throwable throwable) {
    return createError(error + ", cause " + throwable.getMessage());
  }

  @NotNull
  public static FetchAvailableToolsResult join(@NotNull Collection<FetchAvailableToolsResult> results) {
    final TreeSet<DownloadableNuGetTool> joinedTools = new TreeSet<DownloadableNuGetTool>(COMPARATOR);
    final Collection<String> joinedErrors = new ArrayList<String>();
    for (FetchAvailableToolsResult result : results) {
      joinedTools.addAll(result.getFetchedTools());
      joinedErrors.addAll(result.getErrors());
    }
    return new FetchAvailableToolsResult(Collections.unmodifiableSet(joinedTools), Collections.unmodifiableCollection(joinedErrors));
  }

  @NotNull
  public Set<DownloadableNuGetTool> getFetchedTools() {
    return myFetchedTools;
  }

  @NotNull
  public Collection<String> getErrors() {
    return myErrors;
  }

  @Nullable
  public String getErrorsSummary() {
    return myErrors.isEmpty() ? null : StringUtils.join(myErrors.iterator(), "\n");
  }
}