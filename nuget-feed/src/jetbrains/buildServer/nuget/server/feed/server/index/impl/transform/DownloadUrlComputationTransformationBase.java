package jetbrains.buildServer.nuget.server.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.server.feed.server.index.impl.NuGetPackageBuilder;import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageTransformation;import org.jetbrains.annotations.NotNull;import org.jetbrains.annotations.Nullable; /**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.01.12 15:13
 */
public abstract class DownloadUrlComputationTransformationBase implements PackageTransformation {
@Nullable
  protected abstract String getDownloadUrl(@NotNull NuGetPackageBuilder builder);@NotNull
  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    final String downloadUrl = getDownloadUrl(builder);
    if (downloadUrl == null) return Status.SKIP;

    builder.setDownloadUrl(downloadUrl);
    return Status.CONTINUE;
  }}
