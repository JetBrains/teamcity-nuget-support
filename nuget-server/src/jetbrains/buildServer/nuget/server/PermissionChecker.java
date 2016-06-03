/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server;

import jetbrains.buildServer.serverSide.auth.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.08.11 20:51
 */
public class PermissionChecker {
  private final SecurityContext myContext;

  public PermissionChecker(@NotNull final SecurityContext context) {
    myContext = context;
  }

  public boolean hasAccess(@NotNull AuthorityHolder authorityHolder) {
    return AuthUtil.hasGlobalPermission(authorityHolder, Permission.CHANGE_SERVER_SETTINGS);
  }

  public boolean hasAccess() {
    return hasAccess(myContext.getAuthorityHolder());
  }

  public void assertAccess(AuthorityHolder holder) {
    if (!hasAccess(holder)) {
      throw new AccessDeniedException(holder, "You do not have access to view or update NuGet server settings.");
    }
  }
}
