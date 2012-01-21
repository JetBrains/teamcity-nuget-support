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

package jetbrains.buildServer.nuget.server.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.NuGetPackageBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.01.12 20:29
 */
public class DownloadUrlComputationTransformation extends DownloadUrlComputationTransformationBase {
  @Override
  @Nullable
  protected String getDownloadUrl(@NotNull NuGetPackageBuilder builder) {
    String relPath = builder.getMetadata().get(PackagesIndex.TEAMCITY_ARTIFACT_RELPATH);
    final String buildTypeId = builder.getBuildTypeId();
    if (relPath == null || buildTypeId == null) {
      return null;
    }

    while (relPath.startsWith("/")) relPath = relPath.substring(1);
    return "/repository/download/" + buildTypeId + "/" + builder.getBuildId() + ":id/" + relPath;
  }

}
