

package jetbrains.buildServer.nuget.feed.server.olingo.model;

import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * NuGet feed package v1.
 */
public class V1FeedPackage implements Comparable<V1FeedPackage> {
  private final Long myCurrentDate = new Date().getTime();
  private final String myId;
  private final String myVersion;
  private String myAuthors;
  private String myCopyright;
  private Long myCreated = myCurrentDate;
  private String myDependencies;
  private String myDescription;
  private int myDownloadCount;
  private String myExternalPackageUrl;
  private String myGalleryDetailsUrl;
  private String myIconUrl;
  private boolean myIsLatestVersion;
  private String myLanguage;
  private Long myLastUpdated = myCurrentDate;
  private String myLicenseUrl;
  private String myPackageHash;
  private String myPackageHashAlgorithm;
  private long myPackageSize;
  private String myProjectUrl;
  private Long myPublished = myCurrentDate;
  private String myReportAbuseUrl;
  private boolean myRequireLicenseAcceptance;
  private String myReleaseNotes;
  private String mySummary;
  private String myTags;
  private String myTitle;
  private int myVersionDownloadCount;
  private double myRating;
  private String myTeamCityDownloadUrl;
  private final String myContentType = "application/zip";

  public V1FeedPackage(@NotNull String id, @NotNull String version) {
    myId = id;
    myVersion = version;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @NotNull
  public String getVersion() {
    return myVersion;
  }

  @Nullable
  public String getAuthors() {
    return myAuthors;
  }

  public void setAuthors(String authors) {
    myAuthors = authors;
  }

  @Nullable
  public String getCopyright() {
    return myCopyright;
  }

  public void setCopyright(String copyright) {
    myCopyright = copyright;
  }

  @NotNull
  public Long getCreated() {
    return myCreated;
  }

  public void setCreated(@NotNull Long created) {
    myCreated = created;
  }

  @Nullable
  public String getDependencies() {
    return myDependencies;
  }

  public void setDependencies(String dependencies) {
    myDependencies = dependencies;
  }

  @Nullable
  public String getDescription() {
    return myDescription;
  }

  public void setDescription(final String description) {
    myDescription = description;
  }

  public int getDownloadCount() {
    return myDownloadCount;
  }

  public void setDownloadCount(int downloadCount) {
    myDownloadCount = downloadCount;
  }

  @Nullable
  public String getExternalPackageUrl() {
    return myExternalPackageUrl;
  }

  public void setExternalPackageUrl(String externalPackageUrl) {
    myExternalPackageUrl = externalPackageUrl;
  }

  @Nullable
  public String getGalleryDetailsUrl() {
    return myGalleryDetailsUrl;
  }

  public void setGalleryDetailsUrl(String galleryDetailsUrl) {
    myGalleryDetailsUrl = galleryDetailsUrl;
  }

  @Nullable
  public String getIconUrl() {
    return myIconUrl;
  }

  public void setIconUrl(final String iconUrl) {
    myIconUrl = iconUrl;
  }

  public boolean getIsLatestVersion() {
    return myIsLatestVersion;
  }

  public void setIsLatestVersion(boolean isLatestVersion) {
    myIsLatestVersion = isLatestVersion;
  }

  @Nullable
  public String getLanguage() {
    return myLanguage;
  }

  public void setLanguage(String language) {
    myLanguage = language;
  }

  @NotNull
  public Long getLastUpdated() {
    return myLastUpdated;
  }

  public void setLastUpdated(@NotNull Long lastUpdated) {
    myLastUpdated = lastUpdated;
  }

  @Nullable
  public String getLicenseUrl() {
    return myLicenseUrl;
  }

  public void setLicenseUrl(String licenseUrl) {
    myLicenseUrl = licenseUrl;
  }

  @Nullable
  public String getPackageHash() {
    return myPackageHash;
  }

  public void setPackageHash(String packageHash) {
    myPackageHash = packageHash;
  }

  @Nullable
  public String getPackageHashAlgorithm() {
    return myPackageHashAlgorithm;
  }

  public void setPackageHashAlgorithm(String packageHashAlgorithm) {
    myPackageHashAlgorithm = packageHashAlgorithm;
  }

  public long getPackageSize() {
    return myPackageSize;
  }

  public void setPackageSize(long packageSize) {
    myPackageSize = packageSize;
  }

  @Nullable
  public String getProjectUrl() {
    return myProjectUrl;
  }

  public void setProjectUrl(String projectUrl) {
    myProjectUrl = projectUrl;
  }

  @NotNull
  public Long getPublished() {
    return myPublished;
  }

  public void setPublished(@NotNull Long published) {
    myPublished = published;
  }

  @Nullable
  public String getReportAbuseUrl() {
    return myReportAbuseUrl;
  }

  public void setReportAbuseUrl(String reportAbuseUrl) {
    myReportAbuseUrl = reportAbuseUrl;
  }

  public boolean getRequireLicenseAcceptance() {
    return myRequireLicenseAcceptance;
  }

  public void setRequireLicenseAcceptance(boolean requireLicenseAcceptance) {
    myRequireLicenseAcceptance = requireLicenseAcceptance;
  }

  @Nullable
  public String getReleaseNotes() {
    return myReleaseNotes;
  }

  public void setReleaseNotes(String releaseNotes) {
    myReleaseNotes = releaseNotes;
  }

  @Nullable
  public String getSummary() {
    return mySummary;
  }

  public void setSummary(String summary) {
    mySummary = summary;
  }

  @Nullable
  public String getTags() {
    return myTags;
  }

  public void setTags(String tags) {
    myTags = tags;
  }

  @Nullable
  public String getTitle() {
    return myTitle;
  }

  public void setTitle(String title) {
    myTitle = title;
  }

  public int getVersionDownloadCount() {
    return myVersionDownloadCount;
  }

  public void setVersionDownloadCount(int versionDownloadCount) {
    myVersionDownloadCount = versionDownloadCount;
  }

  public double getRating() {
    return myRating;
  }

  public void setRating(double rating) {
    myRating = rating;
  }

  @NotNull
  public String getTeamCityDownloadUrl() {
    return myTeamCityDownloadUrl;
  }

  public void setTeamCityDownloadUrl(@NotNull String teamCityDownloadUrl) {
    myTeamCityDownloadUrl = teamCityDownloadUrl;
  }

  @NotNull
  public String getContentType() {
    return myContentType;
  }

  @Override
  public int compareTo(@NotNull V1FeedPackage o) {
    int idCompare = getId().compareTo(o.getId());
    if (idCompare != 0) return idCompare;

    SemanticVersion v1 = SemanticVersion.valueOf(getVersion());
    SemanticVersion v2 = SemanticVersion.valueOf(o.getVersion());
    if (v1 != null && v2 != null) {
      return v1.compareTo(v2);
    }

    return 0;
  }
}
