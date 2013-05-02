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

import jetbrains.buildServer.nuget.server.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageTransformation;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.01.12 20:30
 */
public class AccessCheckTransformation implements PackageTransformation {
  private final ProjectManager myProjects;
  private final SecurityContext myContext;

  public AccessCheckTransformation(@NotNull final ProjectManager projects,
                                   @NotNull final SecurityContext context) {
    myProjects = projects;
    myContext = context;
  }

  @Nullable
  private String safeFindProjectId(@NotNull final String buildTypeId) {
    try {
      return myProjects.findProjectId(buildTypeId);
    } catch (RuntimeException e) {
      return null;
    }
  }

  private boolean isAccessible(@Nullable final String buildTypeId) {
    if (buildTypeId == null) return false;
    //TODO: move it into BuildMetadataStorage instead.
    //check access to the entry
    final String projectId = safeFindProjectId(buildTypeId);
    //no project no chance
    if (projectId == null) return false;
    //check project access
    return AuthUtil.hasReadAccessTo(myContext.getAuthorityHolder(), projectId);
  }

  @NotNull
  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    return isAccessible(builder.getBuildTypeId()) ? Status.CONTINUE : Status.SKIP;
  }

  @NotNull
  public PackageTransformation createCopy() {
    return this;
  }
}
