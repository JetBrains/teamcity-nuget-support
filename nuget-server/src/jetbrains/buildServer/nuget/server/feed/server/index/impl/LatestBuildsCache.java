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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 30.12.11 19:16
*/
public class LatestBuildsCache {
  @NotNull
  private final ProjectManager myProjectManager;
  @NotNull private final Map<String, Long> myBuildTypeToLatest = new HashMap<String, Long>();

  public LatestBuildsCache(@NotNull final ProjectManager projectManager) {
    myProjectManager = projectManager;
  }

  @Nullable
  public Boolean isLatest(@Nullable final String buildTypeId, final long buildId) {
    if (buildTypeId == null) return null;
    Long build = myBuildTypeToLatest.get(buildTypeId);
    if (build == null) {
      final SBuildType buildTypeById = safeFindBuildTypeById(buildTypeId);
      //skip project if no build type found
      if (buildTypeById == null) return null;

      final SFinishedBuild lastChangesFinished = buildTypeById.getLastChangesFinished();
      //no latest build found, than skip this build
      if (lastChangesFinished == null) return null;

      myBuildTypeToLatest.put(buildTypeId, build = lastChangesFinished.getBuildId());
    }
    return build == buildId;
  }

  @Nullable
  private SBuildType safeFindBuildTypeById(@NotNull final String buildTypeId) {
    try {
      return myProjectManager.findBuildTypeById(buildTypeId);
    } catch (RuntimeException e) {
      ///AccessDeniedException could be thrown
      return null;
    }
  }
}
