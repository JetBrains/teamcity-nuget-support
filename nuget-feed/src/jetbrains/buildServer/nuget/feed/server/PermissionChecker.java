

package jetbrains.buildServer.nuget.feed.server;

import jetbrains.buildServer.serverSide.ProjectNotFoundException;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.*;
import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.08.11 20:51
 */
public class PermissionChecker {

  public PermissionChecker() {
  }

  public void assertAccess(@Nullable final SProject project, @NotNull final AuthorityHolder holder) {
    if (project == null) {
      throw new ProjectNotFoundException("Project id was not specified");
    }

    if (!AuthUtil.hasProjectPermission(holder, project.getProjectId(), Permission.EDIT_PROJECT)) {
      throw new AccessDeniedException(holder, "You do not have access to view or update NuGet server settings.");
    }
  }

  public void checkViewPermissions(@NotNull SUser user, @NotNull SProject project) {
    try {
      // The old way to check permission - allow view server settings only
      if (!AuthUtil.hasGlobalPermission(user, Permission.VIEW_SERVER_SETTINGS)) {
        throw new AccessDeniedException(user, "You do not have access to view or update NuGet server settings.");
      }
    }
    catch (AccessDeniedException e) {
      // Maybe user has edit permission for the particular project? Allow this, too
      // Need this after the fix of TW-86522, as permissions are now checked on includes, too
      assertAccess(project, user);
    }

  }
}
