

package jetbrains.buildServer.nuget.common;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

/**
 * Represent list of downloaded dependecies of a build
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 11:41
 */
public class PackageDependencies {
  private final Collection<NuGetPackageInfo> myUsedPackages;
  private final Collection<NuGetPackageInfo> myCreatedPackages;
  private final Collection<SourcePackageInfo> myPublishedPackages;

  public PackageDependencies(@NotNull final Collection<? extends NuGetPackageInfo> usedPackages,
                             @NotNull final Collection<? extends NuGetPackageInfo> createdPackages,
                             @NotNull final Collection<? extends SourcePackageInfo> publishedPackages) {
    myUsedPackages = Collections.unmodifiableCollection(new TreeSet<NuGetPackageInfo>(usedPackages));
    myCreatedPackages = Collections.unmodifiableCollection(new TreeSet<NuGetPackageInfo>(createdPackages));
    myPublishedPackages = Collections.unmodifiableCollection(new TreeSet<SourcePackageInfo>(publishedPackages));
  }

  /**
   * @return sorted list of packages that were used in project
   */
  @NotNull
  public Collection<NuGetPackageInfo> getUsedPackages() {
    return myUsedPackages;
  }

  /**
   * @return sorted list of packages that were used in project
   */
  @NotNull
  public Collection<NuGetPackageInfo> getCreatedPackages() {
    return myCreatedPackages;
  }

  /**
   * @return sorted list of packages that were published from a build
   */
  @NotNull
  public Collection<SourcePackageInfo> getPublishedPackages() {
    return myPublishedPackages;
  }

  @Override
  public String toString() {
    return "PackageDependencies{" +
            "myUsedPackaged=" + myUsedPackages +
            ", myCreatedPackages=" + myCreatedPackages +
            ", myPublishedPackages=" + myPublishedPackages +
            '}';
  }
}
