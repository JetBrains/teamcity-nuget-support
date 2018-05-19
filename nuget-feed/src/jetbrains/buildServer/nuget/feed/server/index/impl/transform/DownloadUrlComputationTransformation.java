/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.common.index.PackageConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackageTransformation;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:29
*/
public class DownloadUrlComputationTransformation implements PackageTransformation {

  public DownloadUrlComputationTransformation() {
  }

  @NotNull
  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    String relPath = builder.getMetadata().get(PackageConstants.TEAMCITY_ARTIFACT_RELPATH);
    final String buildTypeExternalId = builder.getBuildTypeExternalId();
    if (relPath == null) return Status.SKIP;
    if (buildTypeExternalId == null) return Status.SKIP;

    while (relPath.startsWith("/")) relPath = relPath.substring(1);
    relPath = StringUtil.replace(relPath, "+", "%2B");
    final String downloadUrl = NuGetServerSettings.PATH_PREFIX +
      "/download/" + buildTypeExternalId + "/" + builder.getBuildId() + ":id/" + relPath;
    builder.setDownloadUrl(downloadUrl);
    return Status.CONTINUE;
  }

  @NotNull
  public PackageTransformation createCopy() {
    return this;
  }
}
