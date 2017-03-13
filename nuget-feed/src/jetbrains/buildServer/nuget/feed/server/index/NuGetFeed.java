package jetbrains.buildServer.nuget.feed.server.index;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackagesIndexImpl;
import jetbrains.buildServer.nuget.feed.server.index.impl.SemanticVersionsComparators;
import jetbrains.buildServer.nuget.server.version.FrameworkConstraints;
import jetbrains.buildServer.nuget.server.version.SemanticVersion;
import jetbrains.buildServer.nuget.server.version.VersionConstraint;
import jetbrains.buildServer.nuget.server.version.VersionUtility;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.ID;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.IS_PRERELEASE;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.VERSION;

/**
 * Provides NuGet feed capabilities.
 */
public class NuGetFeed {

  private static final Logger LOG = Logger.getInstance(NuGetFeed.class.getName());
  private static final String VALUE_SEPARATOR = "|";
  private final PackagesIndex myIndex;
  private final NuGetServerSettings myServerSettings;

  public NuGetFeed(@NotNull final PackagesIndex index, @NotNull final NuGetServerSettings serverSettings) {
    myIndex = index;
    myServerSettings = serverSettings;
  }

  @NotNull
  public List<NuGetIndexEntry> getAll() {
    return myIndex.getAll();
  }

  @NotNull
  public List<NuGetIndexEntry> find(@NotNull final Map<String, String> query) {
    return myIndex.find(query);
  }

  @NotNull
  public List<NuGetIndexEntry> search(@NotNull final String searchTerm,
                                      @NotNull final String targetFramework,
                                      final boolean includePrerelease) {
    final boolean filterByTargetFramework = myServerSettings.isFilteringByTargetFrameworkEnabled();

    final Set<String> requestedFrameworks = FrameworkConstraints.convertFromString(targetFramework);
    final StringBuilder builder = new StringBuilder(String.format("Searching packages by term '%s'", searchTerm));
    if (includePrerelease) builder.append(", including pre-release packages");
    if (!requestedFrameworks.isEmpty())
      builder.append(", with target framework constraints ").append(requestedFrameworks);
    final String searchDetails = builder.toString();
    LOG.debug(searchDetails);

    final List<NuGetIndexEntry> foundPackages = searchPackages(searchTerm, filterByTargetFramework ? targetFramework : "");
    final List<NuGetIndexEntry> packages = CollectionsUtil.filterCollection(foundPackages, nugetPackage -> {
      final Map<String, String> nugetPackageAttributes = nugetPackage.getAttributes();
      final String id = nugetPackageAttributes.get(ID);
      final String version = nugetPackageAttributes.get(VERSION);
      final String isPrerelease = nugetPackageAttributes.get(IS_PRERELEASE);
      final String frameworkConstraints = nugetPackageAttributes.get(PackagesIndex.TEAMCITY_FRAMEWORK_CONSTRAINTS);
      if (!includePrerelease && Boolean.parseBoolean(isPrerelease)) {
        LOG.debug(String.format("Skipped package (id:%s, version:%s) since its pre-released.", id, version));
        return false;
      }

      if (filterByTargetFramework) {
        final Set<String> packageFrameworkConstraints = FrameworkConstraints.convertFromString(frameworkConstraints);
        if (!VersionUtility.isPackageCompatibleWithFrameworks(requestedFrameworks, packageFrameworkConstraints)) {
          LOG.debug(String.format("Skipped package (id:%s, version:%s) since it doesn't match requested framework constraints.", id, version));
          return false;
        }
      }

      return true;
    });

    LOG.debug(String.format("Found %d packages while " + searchDetails, packages.size()));
    return packages;
  }

  @NotNull
  private List<NuGetIndexEntry> searchPackages(final String searchTerm, final String targetFramework) {
    if (StringUtil.isEmpty(searchTerm) && StringUtil.isNotEmpty(targetFramework)) {
      return myIndex.search(Collections.singletonList(PackagesIndex.TEAMCITY_FRAMEWORK_CONSTRAINTS), targetFramework);
    }

    return myIndex.search(PackagesIndexImpl.PACKAGE_ATTRIBUTES_TO_SEARCH, searchTerm);
  }

  @NotNull
  public List<NuGetIndexEntry> findPackagesById(@NotNull final String id) {
    final List<NuGetIndexEntry> packages = myIndex.find(CollectionsUtil.asMap(ID, id));
    LOG.debug(String.format("Found %s packages for id %s", packages.size(), id));

    return packages;
  }

  public List<NuGetIndexEntry> getUpdates(@NotNull final String packageIdsValue,
                                          @NotNull final String versionsValue,
                                          @NotNull final String versionConstraintsValue,
                                          @NotNull final String targetFrameworksValue,
                                          final boolean includePrerelease,
                                          final boolean includeAllVersions) {
    final List<String> packageIds = StringUtil.split(packageIdsValue, VALUE_SEPARATOR);
    final List<String> versions = StringUtil.split(versionsValue, VALUE_SEPARATOR);
    final List<String> versionConstraints = StringUtil.split(versionConstraintsValue, VALUE_SEPARATOR);
    final Set<String> targetFrameworks = new HashSet<>(StringUtil.split(targetFrameworksValue, VALUE_SEPARATOR));

    if (packageIds.size() != versions.size()) {
      return Collections.emptyList();
    }

    final List<NuGetIndexEntry> packages = new ArrayList<>();
    for (int i = 0; i < packageIds.size(); i++) {
      final String requestedPackageId = packageIds.get(i);
      final String versionString = versions.get(i);
      final SemanticVersion requestedVersion = SemanticVersion.valueOf(versionString);
      if (requestedVersion == null) {
        LOG.warn("Failed to create valid semantic version from string " + versionString);
        continue;
      }

      VersionConstraint versionConstraint = null;
      if (i < versionConstraints.size()) {
        final String versionConstraintString = versionConstraints.get(i);
        versionConstraint = VersionConstraint.valueOf(versionConstraintString);
        if (versionConstraint == null) {
          LOG.warn("Failed to create valid version constraint from string " + versionConstraintString);
        }
      }

      packages.addAll(getUpdateOfPackageWithId(includeAllVersions, includePrerelease, targetFrameworks, requestedPackageId, requestedVersion, versionConstraint));
    }

    LOG.debug(String.format("%d updated package(s) found", packages.size()));
    return packages;
  }

  private Collection<NuGetIndexEntry> getUpdateOfPackageWithId(final boolean includeAllVersions,
                                                               final boolean includePreRelease,
                                                               final Set<String> frameworkConstraints,
                                                               final String requestedPackageId,
                                                               final SemanticVersion requestedVersion,
                                                               final VersionConstraint versionConstraint) {
    final Comparator<NuGetIndexEntry> comparator = Collections.reverseOrder(SemanticVersionsComparators.getEntriesComparator());
    final List<NuGetIndexEntry> result = new SortedList<>(comparator);

    for (NuGetIndexEntry indexEntry : myIndex.find(CollectionsUtil.asMap(ID, requestedPackageId))) {
      if (match(indexEntry, requestedVersion, includePreRelease, frameworkConstraints, versionConstraint)) {
        LOG.debug(String.format("Matched indexed package found for id:%s version:%s. %s", requestedPackageId, requestedVersion, indexEntry));
        result.add(indexEntry);
      }
    }

    if (includeAllVersions) {
      return result;
    }

    if (result.isEmpty()) {
      return Collections.emptyList();
    }

    return Collections.singletonList(result.get(0));
  }

  private boolean match(final NuGetIndexEntry indexEntry,
                        final SemanticVersion requestedVersion,
                        final boolean includePreRelease,
                        final Set<String> targetFrameworks,
                        final VersionConstraint versionConstraint) {
    final Map<String, String> indexEntryAttributes = indexEntry.getAttributes();
    if (!includePreRelease && Boolean.parseBoolean(indexEntryAttributes.get(IS_PRERELEASE))) {
      return false;
    }

    if (myServerSettings.isFilteringByTargetFrameworkEnabled()) {
      final String frameworkConstraints = indexEntryAttributes.get(PackagesIndex.TEAMCITY_FRAMEWORK_CONSTRAINTS);
      final Set<String> packageFrameworkConstraints = FrameworkConstraints.convertFromString(frameworkConstraints);
      if (!targetFrameworks.isEmpty() && !VersionUtility.isPackageCompatibleWithFrameworks(targetFrameworks, packageFrameworkConstraints)) {
        return false;
      }
    }

    final SemanticVersion entryVersion = SemanticVersion.valueOf(indexEntry.getPackageInfo().getVersion());
    return entryVersion != null &&
      (versionConstraint == null || versionConstraint.satisfies(entryVersion)) &&
      requestedVersion.compareTo(entryVersion) < 0;
  }
}
