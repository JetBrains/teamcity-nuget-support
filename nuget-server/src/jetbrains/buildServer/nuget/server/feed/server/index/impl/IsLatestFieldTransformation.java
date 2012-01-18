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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import jetbrains.buildServer.serverSide.ProjectManager;
import org.jetbrains.annotations.NotNull;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:31
*/
public class IsLatestFieldTransformation implements PackageTransformation {
  private final ProjectManager myProjects;
  private final LatestBuildsCache latestCache;

  public IsLatestFieldTransformation(@NotNull final ProjectManager projects) {
    myProjects = projects;
    latestCache = new LatestBuildsCache(myProjects);
  }

  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    final Boolean isLatestVersion = latestCache.isLatest(builder.getBuildTypeId(), builder.getBuildId());
    if (isLatestVersion == null) {
      return Status.SKIP;
    }
    //TODO: consider semVersions here
    builder.setMetadata("IsLatestVersion", String.valueOf(isLatestVersion));
    builder.setMetadata("IsAbsoluteLatestVersion", String.valueOf(isLatestVersion));
    return Status.CONTINUE;
  }
}
