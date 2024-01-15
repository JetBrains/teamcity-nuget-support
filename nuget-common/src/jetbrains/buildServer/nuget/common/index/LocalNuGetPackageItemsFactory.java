

package jetbrains.buildServer.nuget.common.index;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import jetbrains.buildServer.nuget.common.version.VersionUtility;
import jetbrains.buildServer.nuget.spec.Dependencies;
import jetbrains.buildServer.nuget.spec.Dependency;
import jetbrains.buildServer.nuget.spec.DependencyGroup;
import jetbrains.buildServer.nuget.spec.NuspecFileContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com), re-written by evgeniy.koshkin
 * Date: 06.09.11 22:03
 */
public class LocalNuGetPackageItemsFactory implements NuGetPackageStructureAnalyser {
  private final static int MAX_VALUE_LENGTH = 1024;
  private final Map<String, String> myItems = new LinkedHashMap<String, String>();

  @NotNull
  public Map<String, String> getItems() {
    return myItems;
  }

  public void analyseEntry(@NotNull String entryName) {
  }

  public void analyseNuspecFile(@NotNull NuspecFileContent nuspec) {
    //The list is generated from
    //JetBrains.TeamCity.NuGet.Feed.Tests.DumpRequiredPackageParameters2()
    //not included here: addItem(map, "TeamCityDownloadUrl", "");
    addItem(ID, nuspec.getId());
    final String version = nuspec.getVersion();
    addItem(VERSION, version);
    addItem(NORMALIZED_VERSION, version == null ? null : VersionUtility.normalizeVersion(version));
    addItem(AUTHORS, nuspec.getAuthors());
    addItem(COPYRIGHT, nuspec.getCopyright());
    addItem(DEPENDENCIES, parseDependencies(nuspec));
    addItem(DESCRIPTION, nuspec.getDescription());
    addItem(ICON_URL, nuspec.getIconUrl());
    addItem(IS_PRERELEASE, Boolean.toString(isPrerelease(version)));
    addItem(LANGUAGE, nuspec.getLanguage());
    addItem(PROJECT_URL, nuspec.getProjectUrl());
    addItem(RELEASE_NOTES, nuspec.getReleaseNotes());
    addItem(REQUIRE_LICENSE_ACCEPTANCE, nuspec.getRequireLicenseAcceptance());
    addItem(SUMMARY, nuspec.getSummary());
    addItem(TAGS, nuspec.getTags());
    addItem(TITLE, nuspec.getTitle());
    addItem(MIN_CLIENT_VERSION, nuspec.getMinClientVersion());
    addItem(LICENSE_URL, nuspec.getLicenseUrl());
  }

  private Boolean isPrerelease(String version) {
    if (version == null) {
      return false;
    }

    final SemanticVersion semanticVersion = SemanticVersion.valueOf(version);
    return semanticVersion != null && !StringUtil.isEmpty(semanticVersion.getRelease());
  }

  private void addItem(@NotNull final String key, @Nullable final String value) {
    if (!StringUtil.isEmptyOrSpaces(value)) {
      final int chunks = (int)Math.ceil((double) value.length() / MAX_VALUE_LENGTH);
      for (int i = 0; i < chunks; i++) {
        int index = i * MAX_VALUE_LENGTH;
        String chunkKey = i == 0 ? key : key + i;
        String chunkValue = value.substring(index, Math.min(value.length(), index + MAX_VALUE_LENGTH));
        myItems.put(chunkKey, chunkValue);
      }
    }
  }

  @NotNull
  private String parseDependencies(@NotNull final NuspecFileContent nuspec) {
    final Dependencies dependencies = nuspec.getDependencies();
    if (dependencies == null) return "";
    final StringBuilder sb = new StringBuilder();
    processDependencies(dependencies.getDependencies(), null, sb);
    for (DependencyGroup group : dependencies.getGroups()) {
      processDependencies(group.getDependencies(), group.getTargetFramework(), sb);
    }
    return sb.toString();
  }

  private void processDependencies(@NotNull final Collection<Dependency> dependencies,
                                   @Nullable final String platform,
                                   @NotNull final StringBuilder sb) {
    for (Dependency dependency : dependencies) {
      final String id = dependency.getId();
      final String versionConstraint = dependency.getVersion();
      if (sb.length() != 0) sb.append("|");
      sb.append(id).append(":").append(versionConstraint);
      if (!StringUtil.isEmptyOrSpaces(platform)) {
        sb.append(":").append(platform);
      }
    }
  }
}
