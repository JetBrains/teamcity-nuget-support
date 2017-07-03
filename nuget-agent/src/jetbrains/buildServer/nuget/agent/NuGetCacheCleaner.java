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

  private static final String CLEANER_FORMAT = "Registering directory %s for cleaning";
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
      final File globalPackages = new File(nugetPackages);
      if (globalPackages.isAbsolute() && globalPackages.exists()) {
        LOG.info(String.format(CLEANER_FORMAT, globalPackages));
        registry.addCleaner(globalPackages, new Date());
      }
    }

    final String localAppData = System.getenv("LOCALAPPDATA");
    if (!StringUtil.isEmptyOrSpaces(localAppData)) {
      final File nugetCache = new File(localAppData, "NuGet/Cache");
      if (nugetCache.exists()) {
        LOG.info(String.format(CLEANER_FORMAT, nugetCache));
        registry.addCleaner(nugetCache, new Date());
      }

      final File nugetV3Cache = new File(localAppData, "NuGet/v3-cache");
      if (nugetV3Cache.exists()) {
        LOG.info(String.format(CLEANER_FORMAT, nugetV3Cache));
        registry.addCleaner(nugetV3Cache, new Date());
      }
    }

    final String userHome = System.getProperty("user.home");
    if (!StringUtil.isEmptyOrSpaces(userHome)) {
      final File homeCache = new File(userHome, ".nuget/packages");
      if (homeCache.exists()) {
        LOG.info(String.format(CLEANER_FORMAT, homeCache));
        registry.addCleaner(homeCache, new Date());
      }
    }
  }
}
