

package jetbrains.buildServer.nuget.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.common.version.PackageVersion;
import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackageTransformation;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:31
*/
public class IsLatestFieldTransformation implements PackageTransformation {
  private final Set<String> myReleasedPackages = new HashSet<String>();
  private final Set<String> myAllPackages = new HashSet<String>();

  @NotNull
  public Status applyTransformation(@NotNull final NuGetPackageBuilder builder) {
    final String packageName = builder.getPackageName();
    final PackageVersion version = builder.getVersion();

    //release or preselease version is parsed from package information according for semver.org
    //http://semver.org/
    //http://docs.nuget.org/docs/reference/versioning
    final boolean isReleaseVersion = version instanceof SemanticVersion &&
      StringUtil.isEmpty(((SemanticVersion)version).getRelease());

    //Metadata entries are sorted from newer to older packages
    //isLatestVersion === this is the first occurrence of package in the collection
    final boolean isLatestVersion = isReleaseVersion && myReleasedPackages.add(packageName);
    final boolean isAbsoluteLatestVersion = myAllPackages.add(packageName);

    //here we assume there is a package with full version
    //otherwise there will be a feed with packages without specified IsLatestVersion == true package

    //Note, here we assume the package version is always incremented by the time,
    //Note, thus there is no need to take case about comparison of version
    //Note, i.e. 1.0.0+build ? 1.1.0-release ? 1.0.0 ? 1.1.1 and so on.
    //Note: see https://github.com/NuGet/NuGetGallery/wiki/Package-Metadata-in-the-NuGet-Gallery-Feed
    builder.setIsLatest(isLatestVersion);
    builder.setIsAbsoluteLatest(isAbsoluteLatestVersion);

    return Status.CONTINUE;
  }

  @NotNull
  public PackageTransformation createCopy() {
    return new IsLatestFieldTransformation();
  }
}
