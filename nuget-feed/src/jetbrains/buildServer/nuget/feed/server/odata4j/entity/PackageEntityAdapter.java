

package jetbrains.buildServer.nuget.feed.server.odata4j.entity;

import jetbrains.buildServer.nuget.common.index.ODataDataFormat;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 11.01.12 18:36
 */
public abstract class PackageEntityAdapter extends PackageEntityImpl implements PackageEntity {

  private static final int NO_INFO_DOWNLOAD_COUNT = 0;

  @NotNull
  public LocalDateTime getCreated() {
    final String v = getValue(CREATED);
    if (v != null) {
      final LocalDateTime date = ODataDataFormat.parseDate(v);
      if (date != null) return date;
    }
    return new LocalDateTime();
  }

  @NotNull
  public Integer getDownloadCount() {
    return NO_INFO_DOWNLOAD_COUNT;
  }

  @NotNull
  public String getGalleryDetailsUrl() {
    return getProjectUrl();
  }

  @NotNull
  public LocalDateTime getPublished() {
    final String v = getValue(PUBLISHED);
    if (v != null) {
      final LocalDateTime date = ODataDataFormat.parseDate(v);
      if (date != null) return date;
    }
    return new LocalDateTime();
  }

  @NotNull
  public String getSummary() {
    final String v = getValue(SUMMARY);
    if (v == null) {
      return "";
    }
    return v;
  }

  @NotNull
  public String getTitle() {
    String val = getValue(TITLE);
    if (val != null) return val;
    return getId();
  }

  @NotNull
  public Integer getVersionDownloadCount() {
    return NO_INFO_DOWNLOAD_COUNT;
  }

  @NotNull
  public final LocalDateTime getLastEdited() {
    return getLastUpdated();
  }
}
