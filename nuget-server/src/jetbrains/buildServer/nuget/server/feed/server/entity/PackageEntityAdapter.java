/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.entity;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 11.01.12 18:36
 */
public abstract class PackageEntityAdapter extends PackageEntityImpl implements PackageEntity {
  public PackageEntityAdapter(@NotNull Map<String, String> data) {
    super(data);
  }

  public String getExternalPackageUrl() {
    return getProjectUrl();
  }

  @NotNull
  public Integer getRatingsCount() {
    return 0;
  }

  @NotNull
  public Integer getVersionRatingsCount() {
    return 0;
  }

  @NotNull
  public Double getRating() {
    return 5.0;
  }

  @NotNull
  public Double getVersionRating() {
    return 5.0;
  }

  public String getCategories() {
    return null;
  }

  public String getPackageType() {
    return "Package";
  }

  @NotNull
  public BigDecimal getPrice() {
    return BigDecimal.ZERO;
  }

  @NotNull
  public Boolean getPrerelease() {
    return false;
  }

  @NotNull
  public LocalDateTime getCreated() {
    return getLastUpdated();
  }

  @NotNull
  public Integer getDownloadCount() {
    return 42;
  }

  public String getGalleryDetailsUrl() {
    return getProjectUrl();
  }

  @NotNull
  public LocalDateTime getPublished() {
    return getLastUpdated();
  }

  public String getSummary() {
    return getDescription();
  }

  public String getTitle() {
    return getId();
  }

  @NotNull
  public Integer getVersionDownloadCount() {
    return 42;
  }
}
