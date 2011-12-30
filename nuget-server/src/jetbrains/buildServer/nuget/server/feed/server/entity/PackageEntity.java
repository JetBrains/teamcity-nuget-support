package jetbrains.buildServer.nuget.server.feed.server.entity;

import java.util.*;
import java.lang.*;

public class PackageEntity { 
  private final Map<String, Object> myFields = new HashMap<String, Object>();


  public java.lang.String getId() { 
    return java.lang.String.class.cast(myFields.get("Id"));
  }

  public void setId(final java.lang.String v) { 
    myFields.put("Id", v);
  }


  public java.lang.String getVersion() { 
    return java.lang.String.class.cast(myFields.get("Version"));
  }

  public void setVersion(final java.lang.String v) { 
    myFields.put("Version", v);
  }


  public java.lang.String getAuthors() { 
    return java.lang.String.class.cast(myFields.get("Authors"));
  }

  public void setAuthors(final java.lang.String v) { 
    myFields.put("Authors", v);
  }


  public java.lang.String getCopyright() { 
    return java.lang.String.class.cast(myFields.get("Copyright"));
  }

  public void setCopyright(final java.lang.String v) { 
    myFields.put("Copyright", v);
  }


  public org.joda.time.LocalDateTime getCreated() { 
    return org.joda.time.LocalDateTime.class.cast(myFields.get("Created"));
  }

  public void setCreated(final org.joda.time.LocalDateTime v) { 
    myFields.put("Created", v);
  }


  public java.lang.String getDependencies() { 
    return java.lang.String.class.cast(myFields.get("Dependencies"));
  }

  public void setDependencies(final java.lang.String v) { 
    myFields.put("Dependencies", v);
  }


  public java.lang.String getDescription() { 
    return java.lang.String.class.cast(myFields.get("Description"));
  }

  public void setDescription(final java.lang.String v) { 
    myFields.put("Description", v);
  }


  public java.lang.Integer getDownloadCount() { 
    return java.lang.Integer.class.cast(myFields.get("DownloadCount"));
  }

  public void setDownloadCount(final java.lang.Integer v) { 
    myFields.put("DownloadCount", v);
  }


  public java.lang.String getGalleryDetailsUrl() { 
    return java.lang.String.class.cast(myFields.get("GalleryDetailsUrl"));
  }

  public void setGalleryDetailsUrl(final java.lang.String v) { 
    myFields.put("GalleryDetailsUrl", v);
  }


  public java.lang.String getIconUrl() { 
    return java.lang.String.class.cast(myFields.get("IconUrl"));
  }

  public void setIconUrl(final java.lang.String v) { 
    myFields.put("IconUrl", v);
  }


  public java.lang.Boolean getIsLatestVersion() { 
    return java.lang.Boolean.class.cast(myFields.get("IsLatestVersion"));
  }

  public void setIsLatestVersion(final java.lang.Boolean v) { 
    myFields.put("IsLatestVersion", v);
  }


  public java.lang.Boolean getIsAbsoluteLatestVersion() { 
    return java.lang.Boolean.class.cast(myFields.get("IsAbsoluteLatestVersion"));
  }

  public void setIsAbsoluteLatestVersion(final java.lang.Boolean v) { 
    myFields.put("IsAbsoluteLatestVersion", v);
  }


  public org.joda.time.LocalDateTime getLastUpdated() { 
    return org.joda.time.LocalDateTime.class.cast(myFields.get("LastUpdated"));
  }

  public void setLastUpdated(final org.joda.time.LocalDateTime v) { 
    myFields.put("LastUpdated", v);
  }


  public org.joda.time.LocalDateTime getPublished() { 
    return org.joda.time.LocalDateTime.class.cast(myFields.get("Published"));
  }

  public void setPublished(final org.joda.time.LocalDateTime v) { 
    myFields.put("Published", v);
  }


  public java.lang.String getLanguage() { 
    return java.lang.String.class.cast(myFields.get("Language"));
  }

  public void setLanguage(final java.lang.String v) { 
    myFields.put("Language", v);
  }


  public java.lang.String getLicenseUrl() { 
    return java.lang.String.class.cast(myFields.get("LicenseUrl"));
  }

  public void setLicenseUrl(final java.lang.String v) { 
    myFields.put("LicenseUrl", v);
  }


  public java.lang.String getPackageHash() { 
    return java.lang.String.class.cast(myFields.get("PackageHash"));
  }

  public void setPackageHash(final java.lang.String v) { 
    myFields.put("PackageHash", v);
  }


  public java.lang.String getPackageHashAlgorithm() { 
    return java.lang.String.class.cast(myFields.get("PackageHashAlgorithm"));
  }

  public void setPackageHashAlgorithm(final java.lang.String v) { 
    myFields.put("PackageHashAlgorithm", v);
  }


  public java.lang.Long getPackageSize() { 
    return java.lang.Long.class.cast(myFields.get("PackageSize"));
  }

  public void setPackageSize(final java.lang.Long v) { 
    myFields.put("PackageSize", v);
  }


  public java.lang.String getProjectUrl() { 
    return java.lang.String.class.cast(myFields.get("ProjectUrl"));
  }

  public void setProjectUrl(final java.lang.String v) { 
    myFields.put("ProjectUrl", v);
  }


  public java.lang.String getReportAbuseUrl() { 
    return java.lang.String.class.cast(myFields.get("ReportAbuseUrl"));
  }

  public void setReportAbuseUrl(final java.lang.String v) { 
    myFields.put("ReportAbuseUrl", v);
  }


  public java.lang.String getReleaseNotes() { 
    return java.lang.String.class.cast(myFields.get("ReleaseNotes"));
  }

  public void setReleaseNotes(final java.lang.String v) { 
    myFields.put("ReleaseNotes", v);
  }


  public java.lang.Boolean getRequireLicenseAcceptance() { 
    return java.lang.Boolean.class.cast(myFields.get("RequireLicenseAcceptance"));
  }

  public void setRequireLicenseAcceptance(final java.lang.Boolean v) { 
    myFields.put("RequireLicenseAcceptance", v);
  }


  public java.lang.String getSummary() { 
    return java.lang.String.class.cast(myFields.get("Summary"));
  }

  public void setSummary(final java.lang.String v) { 
    myFields.put("Summary", v);
  }


  public java.lang.String getTags() { 
    return java.lang.String.class.cast(myFields.get("Tags"));
  }

  public void setTags(final java.lang.String v) { 
    myFields.put("Tags", v);
  }


  public java.lang.String getTitle() { 
    return java.lang.String.class.cast(myFields.get("Title"));
  }

  public void setTitle(final java.lang.String v) { 
    myFields.put("Title", v);
  }


  public java.lang.Integer getVersionDownloadCount() { 
    return java.lang.Integer.class.cast(myFields.get("VersionDownloadCount"));
  }

  public void setVersionDownloadCount(final java.lang.Integer v) { 
    myFields.put("VersionDownloadCount", v);
  }


 public boolean isValid() { 
    if (!myFields.containsKey("Id")) return false;
    if (!myFields.containsKey("Version")) return false;
    if (!myFields.containsKey("Authors")) return false;
    if (!myFields.containsKey("Copyright")) return false;
    if (!myFields.containsKey("Created")) return false;
    if (!myFields.containsKey("Dependencies")) return false;
    if (!myFields.containsKey("Description")) return false;
    if (!myFields.containsKey("DownloadCount")) return false;
    if (!myFields.containsKey("GalleryDetailsUrl")) return false;
    if (!myFields.containsKey("IconUrl")) return false;
    if (!myFields.containsKey("IsLatestVersion")) return false;
    if (!myFields.containsKey("IsAbsoluteLatestVersion")) return false;
    if (!myFields.containsKey("LastUpdated")) return false;
    if (!myFields.containsKey("Published")) return false;
    if (!myFields.containsKey("Language")) return false;
    if (!myFields.containsKey("LicenseUrl")) return false;
    if (!myFields.containsKey("PackageHash")) return false;
    if (!myFields.containsKey("PackageHashAlgorithm")) return false;
    if (!myFields.containsKey("PackageSize")) return false;
    if (!myFields.containsKey("ProjectUrl")) return false;
    if (!myFields.containsKey("ReportAbuseUrl")) return false;
    if (!myFields.containsKey("ReleaseNotes")) return false;
    if (!myFields.containsKey("RequireLicenseAcceptance")) return false;
    if (!myFields.containsKey("Summary")) return false;
    if (!myFields.containsKey("Tags")) return false;
    if (!myFields.containsKey("Title")) return false;
    if (!myFields.containsKey("VersionDownloadCount")) return false;
    return true;
  }
}

