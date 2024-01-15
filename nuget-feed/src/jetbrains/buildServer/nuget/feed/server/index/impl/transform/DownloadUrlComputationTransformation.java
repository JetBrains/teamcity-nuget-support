

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

    final String downloadUrl = NuGetServerSettings.PROJECT_PATH + "/" +
      builder.getFeedId() +
      "/download/" +
      buildTypeExternalId +
      "/" +
      builder.getBuildId() +
      ":id/" +
      relPath;
    builder.setDownloadUrl(downloadUrl);

    return Status.CONTINUE;
  }

  @NotNull
  public PackageTransformation createCopy() {
    return this;
  }
}
