

package jetbrains.buildServer.nuget.feed.server.index;

import jetbrains.buildServer.nuget.common.NuGetPackageInfo;
import jetbrains.buildServer.nuget.common.index.PackageConstants;
import jetbrains.buildServer.nuget.common.version.PackageVersion;
import jetbrains.buildServer.nuget.common.version.SemVerLevel;
import jetbrains.buildServer.nuget.common.version.VersionUtility;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 19:29
 */
public class NuGetIndexEntry {

  private final NuGetFeedData myFeedData;
  private final String myKey;
  private final PackageVersion myVersion;
  private final Map<String, String> myAttributes;
  private NuGetPackageInfo myPackageInfo;
  private Boolean mySemanticVersion2;
  private static Pattern VERSIONS = Pattern.compile("\\[?([^\\,\\s)]+)");

  public NuGetIndexEntry(@NotNull final NuGetFeedData feedData,
                         @NotNull final String key,
                         @NotNull final Map<String, String> attributes) {
    myFeedData = feedData;
    myKey = key;
    myAttributes = attributes;
    myVersion = VersionUtility.valueOf(attributes.get(VERSION));
  }

  public NuGetIndexEntry(@NotNull final NuGetFeedData feedData,
                         @NotNull final String key,
                         @NotNull final PackageVersion version,
                         @NotNull final Map<String, String> attributes) {
    myFeedData = feedData;
    myKey = key;
    myAttributes = attributes;
    myVersion = version;
  }

  @NotNull
  public NuGetFeedData getFeedData() {
    return myFeedData;
  }

  @NotNull
  public String getKey() {
    return myKey;
  }

  @NotNull
  public PackageVersion getVersion() {
    return myVersion;
  }

  public boolean isSemanticVersion2() {
    if (mySemanticVersion2 == null) {
      mySemanticVersion2 = getSemanticVersion2();
    }

    return mySemanticVersion2;
  }

  @NotNull
  public Map<String, String> getAttributes() {
    return myAttributes;
  }

  @NotNull
  public String getPackageDownloadUrl() {
    return myAttributes.get(PackageConstants.TEAMCITY_DOWNLOAD_URL);
  }

  @NotNull
  public NuGetPackageInfo getPackageInfo() {
    if (myPackageInfo == null) {
      myPackageInfo = new NuGetPackageInfo(myAttributes.get(ID), myVersion);
    }
    return myPackageInfo;
  }

  /**
   * Gets a value indicating whether package is semver 2.0.
   * @return true if semver 2.0,o otherwise false.
   */
  private boolean getSemanticVersion2() {
    // A package is defined as a SemVer v2.0.0 package if either of the following statements is true:

    // * The package's version is SemVer v2.0.0 compliant but not SemVer v1.0.0 compliant, as per the above definition.
    if (myVersion.getLevel() == SemVerLevel.V2) {
      return true;
    }

    // * Any of the package's dependency version ranges has a minimum or maximum version that is SemVer v2.0.0 compliant
    // but not SemVer v1.0.0 compliant, as per the above definition; e.g. [1.0.0-alpha.1, ).
    final String dependencies = myAttributes.get(DEPENDENCIES);
    if (!StringUtil.isEmpty(dependencies)) {
      for (String dependency : StringUtil.split(dependencies, "|")) {
        final List<String> parts = StringUtil.split(dependency, ":");
        if (parts.size() != 3) continue;
        final Matcher matcher = VERSIONS.matcher(parts.get(1));
        while (matcher.find()) {
          final PackageVersion version = VersionUtility.valueOf(matcher.group(1));
          if (version.getLevel() == SemVerLevel.V2) {
            return true;
          }
        }
      }
    }

    return false;
  }

  @Override
  public String toString() {
    return "NuGetIndexEntry{" +
            "myKey='" + myKey + '\'' +
            '}';
  }
}
