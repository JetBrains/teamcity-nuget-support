

package jetbrains.buildServer.nuget.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.common.index.ODataDataFormat;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackageTransformation;
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes;
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

    final String created = ODataDataFormat.formatDate(build.getFinishDate());
    builder.setMetadata(NuGetPackageAttributes.CREATED, created);
    builder.setMetadata(NuGetPackageAttributes.LAST_UPDATED, created);
    builder.setMetadata(NuGetPackageAttributes.PUBLISHED, created);

    return build.getBuildTypeId();
  }

  @NotNull
  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    if (builder.getBuildTypeId() == null) {
      final String buildTypeId = findBuildTypeId(builder);
      if (buildTypeId == null) return Status.SKIP;

      builder.setBuildTypeId(buildTypeId);
    }
    return Status.CONTINUE;
  }

  @NotNull
  public PackageTransformation createCopy() {
    return this;
  }
}
