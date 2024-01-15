

package jetbrains.buildServer.nuget.agent.dependencies;

import jetbrains.buildServer.nuget.common.PackageDependencies;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 22:43
 */
public interface NuGetPackagesCollector {

  /**
   * Adds used package to the list of packages
   * @param packageId package Id
   * @param version version
   * @param allowedVersions version constraint
   */
  void addDependenyPackage(@NotNull String packageId,
                           @NotNull String version,
                           @Nullable String allowedVersions);

  /**
   * Adds create package to the list of packages
   * @param packageId package Id
   * @param version version
   */
  void addCreatedPackage(@NotNull String packageId,
                         @NotNull String version);


  /**
   * Adds published package
   * @param packageId package id
   * @param version version
   * @param source source
   */
  void addPublishedPackage(@NotNull String packageId, @NotNull String version, @Nullable String source);

  /**
   * @return sorted list of packages that were registered
   */
  @NotNull
  public PackageDependencies getUsedPackages();
}
