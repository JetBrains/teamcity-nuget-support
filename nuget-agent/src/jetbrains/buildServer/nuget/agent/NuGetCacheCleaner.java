package jetbrains.buildServer.nuget.agent;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentDiskSpaceCleanerExtension;
import jetbrains.buildServer.agent.DirectoryCleanersProvider;
import jetbrains.buildServer.agent.DirectoryCleanersProviderContext;
import jetbrains.buildServer.agent.DirectoryCleanersRegistry;
import jetbrains.buildServer.agent.impl.CleanupParameters;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Cleans up nuget package and cache directories.
 */
public class NuGetCacheCleaner implements DirectoryCleanersProvider, AgentDiskSpaceCleanerExtension {

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
    List<CleanFileInfo> filesToCleanup = getFilesToCleanup();
    for (CleanFileInfo cleanFileInfo : filesToCleanup) {
      registry.addCleaner(cleanFileInfo.getFile(), new Date(cleanFileInfo.getLastUsedTime()));
    }
  }

  private List<CleanFileInfo> getFilesToCleanup() {
    final String nugetPackages = System.getenv("NUGET_PACKAGES");
    List<CleanFileInfo> filesToCleanup = new ArrayList<CleanFileInfo>();
    if (!StringUtil.isEmptyOrSpaces(nugetPackages)) {
      // Overriden packages cache since NuGet 3.0
      final File globalPackages = new File(nugetPackages);
      if (globalPackages.isAbsolute() && globalPackages.exists()) {
        registerV3Cache(filesToCleanup, globalPackages);
      }
    }

    final String localAppData = System.getenv("LOCALAPPDATA");
    if (!StringUtil.isEmptyOrSpaces(localAppData)) {
      // Packages cache up to NuGet 3.0
      final File nugetCache = new File(localAppData, "NuGet/Cache");
      if (nugetCache.exists()) {
        registerCache(filesToCleanup, nugetCache);
      }

      // HTTP response cache
      final File nugetV3Cache = new File(localAppData, "NuGet/v3-cache");
      if (nugetV3Cache.exists()) {
        LOG.info(String.format(CLEANER_FORMAT, nugetV3Cache));
        filesToCleanup.add(new CleanFileInfo(nugetV3Cache, System.currentTimeMillis()));
      }
    }

    final String userHome = System.getProperty("user.home");
    if (!StringUtil.isEmptyOrSpaces(userHome)) {
      // Packages cache since NuGet 3.0
      final File homeCache = new File(userHome, ".nuget/packages");
      if (homeCache.exists()) {
        registerV3Cache(filesToCleanup, homeCache);
      }
    }
    return filesToCleanup;
  }

  @NotNull
  @Override
  public List<File> getCleanedRoots() {
    List<CleanFileInfo> filesToCleanup = getFilesToCleanup();

    sortByLastUsedTime(filesToCleanup);

    return mapToFiles(filesToCleanup);
  }

  @Override
  public void cleanup(@NotNull CleanupParameters cleanupParameters) {
    for (File file : cleanupParameters.getFilesToCleanup()) {
      if (!file.exists()) continue;
      FileUtil.delete(file);
    }
  }

  private void sortByLastUsedTime(List<CleanFileInfo> filesToCleanup) {
    Collections.sort(filesToCleanup, new Comparator<CleanFileInfo>() {
      @Override
      public int compare(CleanFileInfo first, CleanFileInfo second) {
        long firstLastUsedTime = first.getLastUsedTime();
        long secondLastUsedTime = second.getLastUsedTime();
        return (firstLastUsedTime < secondLastUsedTime) ? -1 :
          ((firstLastUsedTime == secondLastUsedTime) ? 0 : 1);
      }
    });
  }

  private List<File> mapToFiles(List<CleanFileInfo> filesToCleanup) {
    List<File> result = new ArrayList<File>();
    for (CleanFileInfo cleanFileInfo : filesToCleanup) {
      result.add(cleanFileInfo.getFile());
    }
    return result;
  }

  // Directory structure is as follows:
  // %package-name%.%package-version%.nupkg
  private static void registerCache(List<CleanFileInfo> registry, File directory) {
    LOG.info(String.format(CLEANER_FORMAT, directory));

    final File[] packages = directory.listFiles();
    if (packages != null) {
      for (File file : packages) {
        if (file.equals(directory)) continue;
        if (file.isDirectory()) continue;

        registry.add(new CleanFileInfo(file, file.lastModified()));
      }
    }
  }

  // Directory structure is as follows:
  // %package-name%/%version%/*.*
  private static void registerV3Cache(List<CleanFileInfo> registry, File directory) {
    LOG.info(String.format(CLEANER_FORMAT, directory));

    final File[] packages = directory.listFiles();
    if (packages != null) {
      for (File file : packages) {
        if (file.equals(directory)) continue;
        if (!file.isDirectory()) continue;

        final File[] versions = file.listFiles();
        if (versions != null) {
          for (File version : versions) {
            registry.add(new CleanFileInfo(version, version.lastModified()));
          }
        }

        registry.add(new CleanFileInfo(file, System.currentTimeMillis()));
      }
    }
  }

  private static class CleanFileInfo {
    private final File myFile;
    private final long myLastUsedTime;

    CleanFileInfo(File myFile, long myLastUsedTime) {
      this.myFile = myFile;
      this.myLastUsedTime = myLastUsedTime;
    }

    public File getFile() {
      return myFile;
    }

    long getLastUsedTime() {
      return myLastUsedTime;
    }
  }
}
