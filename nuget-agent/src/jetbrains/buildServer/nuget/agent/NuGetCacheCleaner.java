package jetbrains.buildServer.nuget.agent;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.DirectoryCleanersProvider;
import jetbrains.buildServer.agent.DirectoryCleanersProviderContext;
import jetbrains.buildServer.agent.DirectoryCleanersRegistry;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;

/**
 * Cleans up nuget package and cache directories.
 */
public class NuGetCacheCleaner implements DirectoryCleanersProvider {

  private static final String CLEANER_FORMAT = "Registering packages in %s for cleaning";
  private static final Logger LOG = Logger.getInstance(NuGetCacheCleaner.class.getName());

  @NotNull
  @Override
  public String getCleanerName() {
    return "NuGet Cache Cleaner";
  }

  @Override
  public void registerDirectoryCleaners(@NotNull final DirectoryCleanersProviderContext context,
                                        @NotNull final DirectoryCleanersRegistry registry) {
    final String nugetPackages = System.getenv("NUGET_PACKAGES");
    if (!StringUtil.isEmptyOrSpaces(nugetPackages)) {
      // Overriden packages cache since NuGet 3.0
      final File globalPackages = new File(nugetPackages);
      if (globalPackages.isAbsolute() && globalPackages.exists()) {
        registerV3Cache(registry, globalPackages);
      }
    }

    final String localAppData = System.getenv("LOCALAPPDATA");
    if (!StringUtil.isEmptyOrSpaces(localAppData)) {
      // Packages cache up to NuGet 3.0
      final File nugetCache = new File(localAppData, "NuGet/Cache");
      if (nugetCache.exists()) {
        registerCache(registry, nugetCache);
      }

      // HTTP response cache
      final File nugetV3Cache = new File(localAppData, "NuGet/v3-cache");
      if (nugetV3Cache.exists()) {
        LOG.info(String.format(CLEANER_FORMAT, nugetV3Cache));
        registry.addCleaner(nugetV3Cache, new Date());
      }
    }

    final String userHome = System.getProperty("user.home");
    if (!StringUtil.isEmptyOrSpaces(userHome)) {
      // Packages cache since NuGet 3.0
      final File homeCache = new File(userHome, ".nuget/packages");
      if (homeCache.exists()) {
        registerV3Cache(registry, homeCache);
      }
    }
  }

  // Directory structure is as follows:
  // %package-name%.%package-version%.nupkg
  private static void registerCache(DirectoryCleanersRegistry registry, File directory) {
    LOG.info(String.format(CLEANER_FORMAT, directory));

    final File[] packages = directory.listFiles();
    if (packages != null) {
      for (File file : packages) {
        if (file.equals(directory)) continue;
        if (file.isDirectory()) continue;

        registry.addCleaner(file, new Date(file.lastModified()));
      }
    }
  }

  // Directory structure is as follows:
  // %package-name%/%version%/*.*
  private static void registerV3Cache(DirectoryCleanersRegistry registry, File directory) {
    LOG.info(String.format(CLEANER_FORMAT, directory));

    final File[] packages = directory.listFiles();
    if (packages != null) {
      for (File file : packages) {
        if (file.equals(directory)) continue;
        if (!file.isDirectory()) continue;

        final File[] versions = file.listFiles();
        if (versions != null) {
          for (File version : versions) {
            registry.addCleaner(version, new Date(version.lastModified()));
          }
        }

        registry.addCleaner(file, new Date());
      }
    }
  }
}
