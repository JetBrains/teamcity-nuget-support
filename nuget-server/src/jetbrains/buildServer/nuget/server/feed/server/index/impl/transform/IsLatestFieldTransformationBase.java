package jetbrains.buildServer.nuget.server.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.server.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageTransformation;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.01.12 15:10
 */
public abstract class IsLatestFieldTransformationBase implements PackageTransformation {
  protected abstract Boolean isLatest(@NotNull NuGetPackageBuilder builder);

  @NotNull
  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    final Boolean isLatestVersion = isLatest(builder);
    if (isLatestVersion == null) {
      return Status.SKIP;
    }
    //TODO: consider semVersions here
    builder.setMetadata("IsLatestVersion", String.valueOf(isLatestVersion));
    builder.setMetadata("IsAbsoluteLatestVersion", String.valueOf(isLatestVersion));
    return Status.CONTINUE;
  }
}
