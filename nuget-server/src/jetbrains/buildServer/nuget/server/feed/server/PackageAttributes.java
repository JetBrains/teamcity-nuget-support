/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server;

/**
 * @author Evgeniy.Koshkin
 */
public class PackageAttributes {
  public static final String ID = "Id";
  public static final String VERSION = "Version";
  public static final String NORMALIZED_VERSION = "NormalizedVersion";
  public static final String AUTHORS = "Authors";
  public static final String COPYRIGHT = "Copyright";
  public static final String DEPENDENCIES = "Dependencies";
  public static final String DESCRIPTION = "Description";
  public static final String ICON_URL = "IconUrl";
  public static final String IS_LATEST_VERSION = "IsLatestVersion";
  public static final String IS_ABSOLUTE_LATEST_VERSION = "IsAbsoluteLatestVersion";
  public static final String IS_PRERELEASE = "IsPrerelease";
  public static final String LANGUAGE = "Language";
  public static final String LAST_UPDATED = "LastUpdated";
  public static final String LICENSE_URL = "LicenseUrl";
  public static final String PACKAGE_HASH = "PackageHash";
  public static final String PACKAGE_HASH_ALGORITHM = "PackageHashAlgorithm";
  public static final String PACKAGE_SIZE = "PackageSize";
  public static final String PROJECT_URL = "ProjectUrl";
  public static final String REPORT_ABUSE_URL = "ReportAbuseUrl";
  public static final String RELEASE_NOTES = "ReleaseNotes";
  public static final String REQUIRE_LICENSE_ACCEPTANCE = "RequireLicenseAcceptance";
  public static final String TAGS = "Tags";
  public static final String MIN_CLIENT_VERSION = "MinClientVersion";
  public static final String LICENSE_NAMES = "LicenseNames";
  public static final String LICENSE_REPORT_URL = "LicenseReportUrl";
}
