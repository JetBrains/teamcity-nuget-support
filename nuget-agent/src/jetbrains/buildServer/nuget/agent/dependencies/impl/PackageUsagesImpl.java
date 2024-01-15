

package jetbrains.buildServer.nuget.agent.dependencies.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.common.NuGetPackageInfo;
import jetbrains.buildServer.nuget.common.PackageInfoLoader;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 23:17
 */
public class PackageUsagesImpl implements PackageUsages {
  private static final Logger LOG = Logger.getInstance(PackageUsagesImpl.class.getName());

  private final NuGetPackagesCollector myCollector;
  private final NuGetPackagesConfigParser myParser;
  private final PackageInfoLoader myLoader;

  public PackageUsagesImpl(@NotNull final NuGetPackagesCollector collector,
                           @NotNull final NuGetPackagesConfigParser parser,
                           @NotNull final PackageInfoLoader loader) {
    myCollector = collector;
    myParser = parser;
    myLoader = loader;
  }

  public void reportInstalledPackages(@NotNull final File packagesConfig) {
    if (!packagesConfig.exists()) {
      LOG.debug("Packages file: " + packagesConfig + " does not exit");
      return;
    }

    try {
      myParser.parseNuGetPackages(packagesConfig, myCollector);
    } catch (IOException e) {
      LOG.warnAndDebugDetails("Failed to parse " + packagesConfig + ". " + e.getMessage(), e);
    }
  }

  public void reportCreatedPackages(@NotNull final Collection<File> packageFiles) {
    for (File file : packageFiles) {
      try {
        NuGetPackageInfo info = myLoader.loadPackageInfo(file);
        myCollector.addCreatedPackage(info.getId(), info.getVersion().toString());
      } catch (PackageLoadException e) {
        LOG.warn("Failed to parse create NuGet package: " + file);
      }
    }
  }

  public void reportPublishedPackage(@NotNull final File packageFile, @Nullable String source) {
    try {
      NuGetPackageInfo info = myLoader.loadPackageInfo(packageFile);
      myCollector.addPublishedPackage(info.getId(), info.getVersion().toString(), source);
    } catch (PackageLoadException e) {
      LOG.warn("Failed to parse create NuGet package: " + packageFile);
    }
  }
}
