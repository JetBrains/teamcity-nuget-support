

/****
****
**** THIS CODE IS GENERATED BY jetbrains.buildServer.nuget.tests.server.entity.EntityGenerator$EntityInterfaceGenerator
**** DO NOT CHANGE!
**** Generated with class jetbrains.buildServer.nuget.tests.server.entity.EntityGenerator
**** 
*****/
package jetbrains.buildServer.nuget.feed.server.odata4j.entity;

import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes;
import org.jetbrains.annotations.NotNull;

public interface PackageEntityV2  { 

  @NotNull
  java.lang.String getId();

  @NotNull
  java.lang.String getVersion();

  @NotNull
  java.lang.String getNormalizedVersion();

  @NotNull
  java.lang.String getAuthors();

  @NotNull
  java.lang.String getCopyright();

  @NotNull
  org.joda.time.LocalDateTime getCreated();

  @NotNull
  java.lang.String getDependencies();

  @NotNull
  java.lang.String getDescription();

  @NotNull
  java.lang.Integer getDownloadCount();

  @NotNull
  java.lang.String getGalleryDetailsUrl();

  @NotNull
  java.lang.String getIconUrl();

  @NotNull
  java.lang.Boolean getIsLatestVersion();

  @NotNull
  java.lang.Boolean getIsAbsoluteLatestVersion();

  @NotNull
  java.lang.Boolean getIsPrerelease();

  @NotNull
  java.lang.String getLanguage();

  @NotNull
  org.joda.time.LocalDateTime getLastUpdated();

  @NotNull
  org.joda.time.LocalDateTime getLastEdited();

  @NotNull
  org.joda.time.LocalDateTime getPublished();

  @NotNull
  java.lang.String getLicenseUrl();

  @NotNull
  java.lang.String getPackageHash();

  @NotNull
  java.lang.String getPackageHashAlgorithm();

  @NotNull
  java.lang.Long getPackageSize();

  @NotNull
  java.lang.String getProjectUrl();

  @NotNull
  java.lang.String getReportAbuseUrl();

  @NotNull
  java.lang.String getReleaseNotes();

  @NotNull
  java.lang.Boolean getRequireLicenseAcceptance();

  @NotNull
  java.lang.String getSummary();

  @NotNull
  java.lang.String getTags();

  @NotNull
  java.lang.String getTitle();

  @NotNull
  java.lang.Integer getVersionDownloadCount();

  @NotNull
  java.lang.String getMinClientVersion();

  @NotNull
  java.lang.String getLicenseNames();

  @NotNull
  java.lang.String getLicenseReportUrl();

  String[] KeyPropertyNames = new String[] { NuGetPackageAttributes.ID, NuGetPackageAttributes.VERSION };
}
