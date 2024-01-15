

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
