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

import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:29
*/
public class OldFormatConvertTransformation implements PackageTransformation {
  private final BuildsManager myBuilds;

  public OldFormatConvertTransformation(@NotNull final BuildsManager builds) {
    myBuilds = builds;
  }

  @Nullable
  private SBuild safeFindBuildInstanceById(long buildId) {
    try {
      return myBuilds.findBuildInstanceById(buildId);
    } catch (RuntimeException e) {
      //AccessDeniedException and others could be thrown
      return null;
    }
  }

  @Nullable
  private String findBuildTypeId(@NotNull final NuGetPackageBuilder builder) {
    final SBuild aBuild = safeFindBuildInstanceById(builder.getBuildId());
    if (aBuild == null || !(aBuild instanceof SFinishedBuild)) return null;
    final SFinishedBuild build = (SFinishedBuild) aBuild;

    builder.setMetadata("LastUpdated", ODataDataFormat.formatDate(build.getFinishDate()));
    return build.getBuildTypeId();
  }

  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    if (builder.getBuildTypeId() == null) {
      final String buildTypeId = findBuildTypeId(builder);
      if (buildTypeId != null) {
        builder.setBuildTypeId(buildTypeId);
      } else {
        return Status.SKIP;
      }
    }
    return Status.CONTINUE;
  }
}
