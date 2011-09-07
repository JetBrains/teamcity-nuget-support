/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.render;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.09.11 1:39
 */
public interface NuGetProperties {
  String getId();
  String getVersion();
  String getTitle();
  String getAuthors();
  String getPackageType();
  String getSummary();
  String getDescription();
  String getCopyright();
  String getPackageHashAlgorithm();
  String getPackageHash();
  long getPackageSize();
  BigDecimal getPrice();
  boolean getRequireLicenseAcceptance();
  boolean getIsLatestVersion();
  String getReleaseNotes();
  double getVersionRating();
  int getVersionRatingsCount();
  int getVersionDownloadCount();
  Date getCreated();
  Date getLastUpdated();
  Date getPublished();
  String getExternalPackageUrl();
  String getProjectUrl();
  String getLicenseUrl();
  String getIconUrl();
  double getRating();
  int getRatingsCount();
  int getDownloadCount();
  String getCategories();
  String getTags();
  String getDependencies();
  String getReportAbuseUrl();
  String getGalleryDetailsUrl();
}
