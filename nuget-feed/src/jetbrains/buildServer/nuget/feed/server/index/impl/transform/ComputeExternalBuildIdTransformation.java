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

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackageTransformation;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.version.ServerVersionHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Created 19.06.13 15:24
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ComputeExternalBuildIdTransformation implements PackageTransformation {
  private final ProjectManager myProjects;

  public ComputeExternalBuildIdTransformation(@NotNull final ProjectManager projects) {
    myProjects = projects;
  }

  @NotNull
  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    final String buildTypeId = builder.getBuildTypeId();
    if (buildTypeId == null) return Status.SKIP;

    if (ServerVersionHolder.getVersion().getDisplayVersionMajor() < 8) {
      //workaround for 7.1.x build of TeamCity to preserve compatibility
      builder.setBuildTypeExternalId(buildTypeId);
      return Status.CONTINUE;
    }

    final SBuildType externalBuildTypeId = myProjects.findBuildTypeById(buildTypeId);
    if (externalBuildTypeId == null) return Status.SKIP;

    builder.setBuildTypeExternalId(externalBuildTypeId.getExternalId());

    return Status.CONTINUE;
  }

  @NotNull
  public PackageTransformation createCopy() {
    return this;
  }
}
