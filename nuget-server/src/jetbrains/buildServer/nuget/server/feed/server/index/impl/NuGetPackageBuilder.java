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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex.*;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:16
*/
public class NuGetPackageBuilder {
  public static final String IS_PRERELEASE = "IsPrerelease";
  public static final String IS_LATEST_VERSION = "IsLatestVersion";
  public static final String IS_ABSOLUTE_LATEST_VERSION = "IsAbsoluteLatestVersion";
  private static final String VERSION = "Version";
  private static final String ID = "Id";

  private final String myKey;
  private final String myVersion;
  private final long myBuildId;
  private final Map<String, String> myMetadata;
  private String myExternalId = null;

  public NuGetPackageBuilder(@NotNull final BuildMetadataEntry entry) {
    myMetadata = new HashMap<String, String>(entry.getMetadata());
    myVersion = myMetadata.get(VERSION);
    myKey = entry.getKey() + "." + myVersion;
    myBuildId = entry.getBuildId();
    setMetadata(TEAMCITY_BUILD_ID, String.valueOf(myBuildId));
  }

  @NotNull
  public String getKey() {
    return myKey;
  }

  public long getBuildId() {
    return myBuildId;
  }

  @NotNull
  public String getPackageName() {
    return myMetadata.get(ID);
  }

  @NotNull
  public String getVersion() {
    return myVersion;
  }

  public void setPrerelease(boolean isPrerelease) {
    setMetadata(IS_PRERELEASE, String.valueOf(isPrerelease));
  }

  public boolean isPrerelease() {
    return Boolean.valueOf(myMetadata.get(IS_PRERELEASE));
  }

  @NotNull
  public Map<String, String> getMetadata() {
    return myMetadata;
  }

  @Nullable
  public String getBuildTypeId() {
    return myMetadata.get(TEAMCITY_BUILD_TYPE_ID);
  }

  @Nullable
  public String getBuildTypeExternalId() {
    return myExternalId;
  }

  public void setBuildTypeId(@NotNull String buildTypeId) {
    setMetadata(TEAMCITY_BUILD_TYPE_ID, buildTypeId);
  }

  public void setBuildTypeExternalId(@NotNull String externalId) {
    myExternalId = externalId;
  }

  public void setDownloadUrl(@NotNull final String downloadUrl) {
    setMetadata(TEAMCITY_DOWNLOAD_URL, downloadUrl);
  }

  @Nullable
  public String getDownloadUrl() {
    return myMetadata.get(TEAMCITY_DOWNLOAD_URL);
  }

  public void setMetadata(@NotNull final String key, @NotNull final String value) {
    myMetadata.put(key, value);
  }

  public void setIsAbsoluteLatest(boolean isAbsoluteLatest) {
    setMetadata(IS_ABSOLUTE_LATEST_VERSION, String.valueOf(isAbsoluteLatest));
  }

  public void setIsLatest(boolean isLatest) {
    setMetadata(IS_LATEST_VERSION, String.valueOf(isLatest));
  }

  @Nullable
  public NuGetIndexEntry build() {
    if (getDownloadUrl() == null) return null;
    if (getBuildTypeId() == null) return null;

    if (myMetadata.get(IS_LATEST_VERSION) == null) setIsLatest(false);
    if (myMetadata.get(IS_ABSOLUTE_LATEST_VERSION) == null) setIsAbsoluteLatest(false);
    return new NuGetIndexEntry(
            myKey,
            myMetadata
    );
  }
}
