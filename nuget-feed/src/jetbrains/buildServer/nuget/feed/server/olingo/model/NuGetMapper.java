/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.olingo.model;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.nuget.common.index.PackageConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetUtils;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.common.index.ODataDataFormat;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.LocalDateTime;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Date;
import java.util.Map;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * NuGet feed mapper.
 */
public final class NuGetMapper {

  /**
   * Maps nuget package entry.
   *
   * @param indexEntry is an entry.
   * @param rootUriWithAuthenticattionType is a root URL with context and authentication type.
   * @return package.
   */
  @Nullable
  public static V2FeedPackage mapPackage(@Nullable final NuGetIndexEntry indexEntry, @NotNull final URI rootUriWithAuthenticattionType) {
    if (indexEntry == null) {
      return null;
    }

    final Map<String, String> attributes = indexEntry.getAttributes();
    final V2FeedPackage feedPackage = new V2FeedPackage(getValue(attributes, ID), getValue(attributes, VERSION));
    feedPackage.setNormalizedVersion(getValue(attributes, NORMALIZED_VERSION));
    feedPackage.setAuthors(getValue(attributes, AUTHORS));
    feedPackage.setCopyright(getValue(attributes, COPYRIGHT));
    feedPackage.setCreated(getDate(attributes, CREATED));
    feedPackage.setDependencies(getValue(attributes, DEPENDENCIES));
    feedPackage.setDescription(getValue(attributes, DESCRIPTION));
    feedPackage.setIconUrl(getValue(attributes, ICON_URL));
    feedPackage.setIsLatestVersion(getBoolean(attributes, IS_LATEST_VERSION));
    feedPackage.setIsAbsoluteLatestVersion(getBoolean(attributes, IS_ABSOLUTE_LATEST_VERSION));
    feedPackage.setIsPrerelease(getBoolean(attributes, IS_PRERELEASE));
    feedPackage.setLanguage(getValue(attributes, LANGUAGE));
    feedPackage.setLastUpdated(getDate(attributes, LAST_UPDATED));
    feedPackage.setPublished(getDate(attributes, PUBLISHED));
    feedPackage.setPackageHash(getValue(attributes, PACKAGE_HASH));
    feedPackage.setPackageHashAlgorithm(getValue(attributes, PACKAGE_HASH_ALGORITHM));
    feedPackage.setPackageSize(getLong(attributes, PACKAGE_SIZE));
    feedPackage.setProjectUrl(getValue(attributes, PROJECT_URL));
    feedPackage.setReportAbuseUrl(getValue(attributes, REPORT_ABUSE_URL));
    feedPackage.setReleaseNotes(getValue(attributes, RELEASE_NOTES));
    feedPackage.setRequireLicenseAcceptance(getBoolean(attributes, REQUIRE_LICENSE_ACCEPTANCE));
    feedPackage.setSummary(getValue(attributes, SUMMARY));
    feedPackage.setTags(getValue(attributes, TAGS));
    feedPackage.setTitle(getValue(attributes, TITLE));
    feedPackage.setMinClientVersion(getValue(attributes, MIN_CLIENT_VERSION));
    feedPackage.setLicenseUrl(getValue(attributes, LICENSE_URL));
    feedPackage.setLicenseNames(getValue(attributes, LICENSE_NAMES));
    feedPackage.setLicenseReportUrl(getValue(attributes, LICENSE_REPORT_URL));
    feedPackage.setTeamCityDownloadUrl(getDownloadUrl(rootUriWithAuthenticattionType, attributes));

    return feedPackage;
  }

  private static String getDownloadUrl(@NotNull URI rootUriWithAuthenticationType, Map<String, String> attributes) {
    final String artifactPath = getValue(attributes, PackageConstants.TEAMCITY_DOWNLOAD_URL);
    return rootUriWithAuthenticationType.toString() + artifactPath;
  }

  private static String getValue(@NotNull final Map<String, String> attributes, @NotNull final String key) {
    return NuGetUtils.getValue(attributes, key);
  }

  @NotNull
  private static Boolean getBoolean(@NotNull final Map<String, String> attributes, @NotNull final String key) {
    return Boolean.parseBoolean(getValue(attributes, key));
  }

  @NotNull
  private static Long getDate(@NotNull final Map<String, String> attributes, @NotNull final String key) {
    final String value = getValue(attributes, key);
    if (value != null) {
      final LocalDateTime dateTime = ODataDataFormat.parseDate(value);
      if (dateTime != null) return dateTime.toDate().getTime();
    }

    return new Date().getTime();
  }

  @NotNull
  private static Long getLong(@NotNull final Map<String, String> attributes, @NotNull final String key) {
    final String value = getValue(attributes, key);
    return value == null ? 0 : Long.parseLong(value);
  }
}
