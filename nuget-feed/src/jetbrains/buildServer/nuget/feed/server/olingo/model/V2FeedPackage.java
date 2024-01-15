

package jetbrains.buildServer.nuget.feed.server.olingo.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * NuGet feed package v1.
 */
public class V2FeedPackage extends V1FeedPackage {

  private String myNormalizedVersion;
  private boolean myIsAbsoluteLatestVersion;
  private boolean myIsPrerelease;
  private String myMinClientVersion;
  private Long myLastEdited;
  private String myLicenseNames;
  private String myLicenseReportUrl;

  public V2FeedPackage(@NotNull String id, @NotNull String version) {
    super(id, version);
  }

  @Nullable
  public String getNormalizedVersion() {
    return myNormalizedVersion;
  }

  public void setNormalizedVersion(String normalizedVersion) {
    myNormalizedVersion = normalizedVersion;
  }

  public boolean getIsAbsoluteLatestVersion() {
    return myIsAbsoluteLatestVersion;
  }

  public void setIsAbsoluteLatestVersion(boolean isAbsoluteLatestVersion) {
    myIsAbsoluteLatestVersion = isAbsoluteLatestVersion;
  }

  public boolean getIsPrerelease() {
    return myIsPrerelease;
  }

  public void setIsPrerelease(boolean isPrerelease) {
    myIsPrerelease = isPrerelease;
  }

  @Nullable
  public String getMinClientVersion() {
    return myMinClientVersion;
  }

  public void setMinClientVersion(String minClientVersion) {
    myMinClientVersion = minClientVersion;
  }

  @Nullable
  public Long getLastEdited() {
    return myLastEdited;
  }

  public void setLastEdited(Long lastEdited) {
    myLastEdited = lastEdited;
  }

  @Nullable
  public String getLicenseNames() {
    return myLicenseNames;
  }

  public void setLicenseNames(String licenseNames) {
    myLicenseNames = licenseNames;
  }

  @Nullable
  public String getLicenseReportUrl() {
    return myLicenseReportUrl;
  }

  public void setLicenseReportUrl(String licenseReportUrl) {
    myLicenseReportUrl = licenseReportUrl;
  }
}
