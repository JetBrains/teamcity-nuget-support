

package jetbrains.buildServer.nuget.feed.server.index.impl;

import jetbrains.buildServer.nuget.common.version.PackageVersion;
import jetbrains.buildServer.nuget.common.version.VersionUtility;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.nuget.common.index.PackageConstants.*;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:16
*/
public class NuGetPackageBuilder {

  private final String myKey;
  private final PackageVersion myVersion;
  private final long myBuildId;
  private final NuGetFeedData myFeedData;
  private final Map<String, String> myMetadata;
  private String myBuildTypeExtId = null;

  public NuGetPackageBuilder(@NotNull final NuGetFeedData feedData, @NotNull final BuildMetadataEntry entry) {
    myFeedData = feedData;
    myMetadata = new HashMap<>(entry.getMetadata());
    myVersion = VersionUtility.valueOf(myMetadata.get(VERSION));
    myKey = entry.getKey();
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

  public String getFeedId() {
    final String projectId = myFeedData.getProjectExtId();
    return String.format("%s/%s", projectId, myFeedData.getFeedId());
  }

  @NotNull
  public String getPackageName() {
    return myMetadata.get(ID);
  }

  @NotNull
  public PackageVersion getVersion() {
    return myVersion;
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
    return myBuildTypeExtId;
  }

  public void setBuildTypeId(@NotNull String buildTypeId) {
    setMetadata(TEAMCITY_BUILD_TYPE_ID, buildTypeId);
  }

  public void setBuildTypeExternalId(@NotNull String externalId) {
    myBuildTypeExtId = externalId;
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
            myFeedData,
            myKey,
            myVersion,
            myMetadata
    );
  }
}
