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

package jetbrains.buildServer.nuget.feed.server;

import jetbrains.buildServer.serverSide.ProjectNotFoundException;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.08.11 20:51
 */
public class PermissionChecker {

  public PermissionChecker() {
  }

  public void assertAccess(@NotNull final AuthorityHolder holder) {
    if (!AuthUtil.hasGlobalPermission(holder, Permission.VIEW_SERVER_SETTINGS)) {
      throw new AccessDeniedException(holder, "You do not have access to view or update NuGet server settings.");
    }
  }

  public void assertAccess(@Nullable final SProject project, @NotNull final AuthorityHolder holder) {
    if (project == null) {
      throw new ProjectNotFoundException("Project id was not specified");
    }

    if (!AuthUtil.hasProjectPermission(holder, project.getProjectId(), Permission.EDIT_PROJECT)) {
      throw new AccessDeniedException(holder, "You do not have access to view or update NuGet server settings.");
    }
  }
}
