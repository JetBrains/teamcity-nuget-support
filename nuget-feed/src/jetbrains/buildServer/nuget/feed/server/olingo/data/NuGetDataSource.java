/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.feed.server.olingo.data;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.feed.server.index.impl.SemanticVersionsComparators;
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes;
import jetbrains.buildServer.nuget.server.version.FrameworkConstraints;
import jetbrains.buildServer.nuget.server.version.SemanticVersion;
import jetbrains.buildServer.nuget.server.version.VersionConstraint;
import jetbrains.buildServer.nuget.server.version.VersionUtility;
import jetbrains.buildServer.util.StringUtil;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.exception.ODataHttpException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.uri.UriSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.IS_PRERELEASE;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.VERSION;

/**
 * Controls request to the data source.
 */
public class NuGetDataSource {

  private static final Logger LOG = Logger.getInstance(NuGetDataSource.class.getName());
  private final PackagesIndex myIndex;
  private final NuGetServerSettings myServerSettings;

  public NuGetDataSource(@NotNull final PackagesIndex index,
                         @NotNull final NuGetServerSettings serverSettings) {
    myIndex = index;
    myServerSettings = serverSettings;
  }

  /**
   * Retrieves complete data set data.
   *
   * @param entitySet is a target entity set.
   * @return data
   */
  @NotNull
  public List<?> readData(@NotNull final EdmEntitySet entitySet) throws ODataHttpException, EdmException {
    if (MetadataConstants.ENTITY_SET_NAME.equals(entitySet.getName())) {
      final List<NuGetIndexEntry> packages = new ArrayList<>();
      final Iterator<NuGetIndexEntry> indexEntries = myIndex.getNuGetEntries();

      while (indexEntries.hasNext()) {
        packages.add(indexEntries.next());
      }

      return packages;
    }

    throw new ODataNotImplementedException();
  }

  /**
   * Retrieves entities with a limited set of properties.
   *
   * @param entitySet is a target entity set
   * @param keys      required proeprties.
   * @return data
   */
  @NotNull
  public Object readData(@NotNull final EdmEntitySet entitySet,
                         @NotNull final Map<String, Object> keys) throws ODataHttpException, EdmException {
    if (MetadataConstants.ENTITY_SET_NAME.equals(entitySet.getName())) {
      final Iterator<NuGetIndexEntry> indexEntries = myIndex.getNuGetEntries();
      if (keys.isEmpty()) {
        final List<NuGetIndexEntry> packages = new ArrayList<>();
        while (indexEntries.hasNext()) {
          packages.add(indexEntries.next());
        }

        return packages;
      }

      final String id = (String) keys.get(NuGetPackageAttributes.ID);
      final String version = (String) keys.get(NuGetPackageAttributes.VERSION);
      while (indexEntries.hasNext()) {
        final NuGetIndexEntry indexEntry = indexEntries.next();
        final Map<String, String> attributes = indexEntry.getAttributes();
        if (id != null) {
          if (!attributes.get(NuGetPackageAttributes.ID).equalsIgnoreCase(id)) {
            continue;
          }
        }

        if (version != null) {
          if (!attributes.get(NuGetPackageAttributes.VERSION).equalsIgnoreCase(version)) {
            continue;
          }
        }

        return indexEntry;
      }

      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
    }

    throw new ODataNotImplementedException();
  }

  /**
   * Retrieves data for function calls.
   *
   * @param function   is a function reference.
   * @param parameters is a list of parameters.
   * @param keys       is a list of keys to select.
   * @return data.
   */
  @NotNull
  public Object readData(@NotNull final EdmFunctionImport function,
                         @NotNull final Map<String, Object> parameters,
                         @Nullable final Map<String, Object> keys) throws ODataHttpException, EdmException {
    if (function.getName().equals(MetadataConstants.SEARCH_FUNCTION_NAME)) {
      return search(parameters, keys);
    } else if (function.getName().equals(MetadataConstants.FIND_PACKAGES_BY_ID_FUNCTION_NAME)) {
      return findPackagesById(parameters, keys);
    } else if (function.getName().equals(MetadataConstants.GET_UPDATES_FUNCTION_NAME) && NuGetAPIVersion.shouldUseV2()) {
      return getUpdates(parameters);
    } else {
      throw new ODataNotImplementedException();
    }
  }

  /**
   * Search function.
   *
   * @param parameters is a parameters.
   * @param keys       is a required keys.
   * @return data.
   */
  private Object search(final Map<String, Object> parameters,
                        final Map<String, Object> keys) throws ODataHttpException {
    final String searchTerm = (String) parameters.get(MetadataConstants.SEARCH_TERM);
    final String targetFramework = (String) parameters.get(MetadataConstants.TARGET_FRAMEWORK);
    if (searchTerm == null || targetFramework == null) {
      throw new UriSyntaxException(UriSyntaxException.MISSINGPARAMETER);
    }

    boolean includePrerelease = NuGetAPIVersion.shouldUseV2() && (Boolean) parameters.get(MetadataConstants.INCLUDE_PRERELEASE);
    final Set<String> requestedFrameworks = FrameworkConstraints.convertFromString(targetFramework);
    String format = String.format("Searching packages by term '%s'", searchTerm);
    if (includePrerelease) format += ", including pre-release packages";
    if (!requestedFrameworks.isEmpty()) format += ", with target framework constraints " + requestedFrameworks;
    LOG.debug(format);

    final List<NuGetIndexEntry> packages = new ArrayList<>();
    final Iterator<NuGetIndexEntry> entryIterator = myIndex.search(searchTerm);

    while (entryIterator.hasNext()) {
      final NuGetIndexEntry nugetPackage = entryIterator.next();
      final Map<String, String> nugetPackageAttributes = nugetPackage.getAttributes();
      final String id = nugetPackageAttributes.get(NuGetPackageAttributes.ID);
      final String version = nugetPackageAttributes.get(VERSION);
      final String isPrerelease = nugetPackageAttributes.get(IS_PRERELEASE);
      final String frameworkConstraints = nugetPackageAttributes.get(PackagesIndex.TEAMCITY_FRAMEWORK_CONSTRAINTS);
      if (!includePrerelease && Boolean.parseBoolean(isPrerelease)) {
        LOG.debug(String.format("Skipped package (id:%s, version:%s) since its pre-released.", id, version));
        continue;
      }

      if (myServerSettings.isFilteringByTargetFrameworkEnabled()) {
        final Set<String> packageFrameworkConstraints = FrameworkConstraints.convertFromString(frameworkConstraints);
        if (!VersionUtility.isPackageCompatibleWithFrameworks(requestedFrameworks, packageFrameworkConstraints)) {
          LOG.debug(String.format("Skipped package (id:%s, version:%s) since it doesn't match requested framework constraints.", id, version));
          continue;
        }
      }

      packages.add(nugetPackage);
    }

    LOG.debug(String.format("Found %d packages while " + format, packages.size()));
    return packages;
  }

  /**
   * Find packages by id function.
   *
   * @param parameters is a parameters.
   * @param keys       is a list of keys to select.
   * @return data.
   */
  private Object findPackagesById(final Map<String, Object> parameters,
                                  final Map<String, Object> keys) throws ODataHttpException {
    final String id = (String) parameters.get(MetadataConstants.ID);
    if (id == null) {
      throw new UriSyntaxException(UriSyntaxException.MISSINGPARAMETER);
    }

    final Iterator<NuGetIndexEntry> indexEntries = myIndex.getNuGetEntries(id);
    final List<NuGetIndexEntry> packages = new ArrayList<>();
    while (indexEntries.hasNext()) {
      packages.add(indexEntries.next());
    }

    LOG.debug(String.format("Found %s packages for id %s", packages.size(), id));
    return packages;
  }


  /**
   * Get updates function.
   *
   * @param parameters is a parameters.
   * @return data.
   */
  private Object getUpdates(final Map<String, Object> parameters) throws ODataHttpException {
    final List<String> packageIds = StringUtil.split((String) parameters.get(MetadataConstants.PACKAGE_IDS), "|");
    final List<String> versions = StringUtil.split((String) parameters.get(MetadataConstants.VERSIONS), "|");
    final List<String> versionConstraints = StringUtil.split((String) parameters.get(MetadataConstants.VERSION_CONSTRAINTS), "|");
    final Set<String> targetFrameworks = new HashSet<>(StringUtil.split((String) parameters.get(MetadataConstants.TARGET_FRAMEWORKS), "|"));
    final boolean includePrerelease = (Boolean) parameters.get(MetadataConstants.INCLUDE_PRERELEASE);
    final boolean includeAllVersions = (Boolean) parameters.get(MetadataConstants.INCLUDE_ALL_VERSIONS);

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
    final Iterator<NuGetIndexEntry> entryIterator = myIndex.getNuGetEntries(requestedPackageId);
    final Comparator<NuGetIndexEntry> comparator = Collections.reverseOrder(SemanticVersionsComparators.getEntriesComparator());
    final List<NuGetIndexEntry> result = new SortedList<>(comparator);

    while (entryIterator.hasNext()) {
      final NuGetIndexEntry indexEntry = entryIterator.next();
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
