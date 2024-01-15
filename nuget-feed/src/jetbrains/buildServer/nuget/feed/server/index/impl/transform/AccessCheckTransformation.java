

package jetbrains.buildServer.nuget.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackageTransformation;
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
