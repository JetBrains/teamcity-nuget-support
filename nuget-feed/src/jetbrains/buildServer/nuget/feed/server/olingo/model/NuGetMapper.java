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

package jetbrains.buildServer.nuget.feed.server.olingo.model;

import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.index.PackagesIndex;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
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
   * @param requestUri is a request URL.
   * @return package.
   */
  @NotNull
  public static V2FeedPackage mapPackage(@NotNull final NuGetIndexEntry indexEntry, URI requestUri) {
    final Map<String, String> attributes = indexEntry.getAttributes();
    final V2FeedPackage feedPackage = new V2FeedPackage(attributes.get(ID), attributes.get(VERSION));
    feedPackage.setNormalizedVersion(attributes.get(NORMALIZED_VERSION));
    feedPackage.setAuthors(attributes.get(AUTHORS));
    feedPackage.setCopyright(attributes.get(COPYRIGHT));
    feedPackage.setDependencies(attributes.get(DEPENDENCIES));
    feedPackage.setDescription(attributes.get(DESCRIPTION));
    feedPackage.setIconUrl(attributes.get(ICON_URL));
    feedPackage.setIsLatestVersion(Boolean.parseBoolean(attributes.get(IS_LATEST_VERSION)));
    feedPackage.setIsAbsoluteLatestVersion(Boolean.parseBoolean(attributes.get(IS_ABSOLUTE_LATEST_VERSION)));
    feedPackage.setIsPrerelease(Boolean.parseBoolean(attributes.get(IS_PRERELEASE)));
    feedPackage.setLanguage(attributes.get(LANGUAGE));
    feedPackage.setPackageHash(attributes.get(PACKAGE_HASH));
    feedPackage.setPackageHashAlgorithm(attributes.get(PACKAGE_HASH_ALGORITHM));
    feedPackage.setPackageSize(Long.parseLong(attributes.get(PACKAGE_SIZE)));
    feedPackage.setProjectUrl(attributes.get(PROJECT_URL));
    feedPackage.setReportAbuseUrl(attributes.get(REPORT_ABUSE_URL));
    feedPackage.setReleaseNotes(attributes.get(RELEASE_NOTES));
    feedPackage.setRequireLicenseAcceptance(Boolean.parseBoolean(attributes.get(REQUIRE_LICENSE_ACCEPTANCE)));
    feedPackage.setTags(attributes.get(TAGS));
    feedPackage.setTitle(attributes.get(TITLE));
    feedPackage.setTeamCityDownloadUrl(requestUri.toString() + attributes.get(PackagesIndex.TEAMCITY_DOWNLOAD_URL));
    feedPackage.setMinClientVersion(attributes.get(MIN_CLIENT_VERSION));
    feedPackage.setLicenseUrl(attributes.get(LICENSE_URL));
    feedPackage.setLicenseNames(attributes.get(LICENSE_NAMES));
    feedPackage.setLicenseReportUrl(attributes.get(LICENSE_REPORT_URL));

    return feedPackage;
  }
}
