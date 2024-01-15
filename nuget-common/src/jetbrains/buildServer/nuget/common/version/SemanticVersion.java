

package jetbrains.buildServer.nuget.common.version;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Semantic package version.
 */
public class SemanticVersion implements PackageVersion {
  private static Pattern VERSION_STRING_MATCHING_PATTERN = Pattern.compile("^([0-9]+)(?:\\.([0-9]+))?(?:\\.([0-9]+))?(?:\\.([0-9]+))?(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(\\+[0-9A-Za-z-\\.]+)?$", Pattern.CASE_INSENSITIVE);

  private final Version myVersion;
  private final String myRelease;
  private final String myMetadata;
  private final String myOriginalString;
  private final SemVerLevel myLevel;

  private SemanticVersion(@NotNull Version version,
                          @Nullable String release,
                          @Nullable String metadata,
                          @NotNull String originalString) {
    myVersion = version;
    myRelease = release;
    myMetadata = metadata;
    myOriginalString = originalString;

    // A version is defined as SemVer v2.0.0 if either of the following statements is true:
    // * The (pre)release label is dot-separated, e.g. 1.0.0-alpha.1
    // * The version has build-metadata, e.g. 1.0.0+githash
    myLevel = StringUtil.isNotEmpty(myMetadata) || StringUtil.isNotEmpty(myRelease) && myRelease.contains(".")
      ? SemVerLevel.V2
      : SemVerLevel.V1;
  }

  @Nullable
  public static SemanticVersion valueOf(@NotNull String versionString) {
    if (StringUtil.isEmpty(versionString)) return null;

    final Matcher match = VERSION_STRING_MATCHING_PATTERN.matcher(versionString.trim());
    if (!match.find()) return null;

    int major = Integer.valueOf(match.group(1));
    final String minorString = match.group(2);
    int minor = (minorString != null) ? Integer.valueOf(minorString) : 0;
    final String patchString = match.group(3);
    int patch = (patchString != null) ? Integer.valueOf(patchString) : 0;
    final String buildString = match.group(4);
    int build = (buildString != null) ? Integer.valueOf(buildString) : 0;

    final Version versionValue = new Version(major, minor, patch, build);

    String release = match.group(5);
    if (release != null && release.startsWith("-"))
      release = release.substring(1);

    String metadata = match.group(6);
    if (metadata != null && metadata.startsWith("+"))
      metadata = metadata.substring(1);

    return new SemanticVersion(versionValue, release, metadata, versionString.replace(" ", ""));
  }

  @NotNull
  public Version getVersion() {
    return myVersion;
  }

  @Nullable
  public String getRelease() {
    return myRelease;
  }

  @Nullable
  public String getMetadata() {
    return myMetadata;
  }

  public int compareTo(@NotNull PackageVersion other) {
    if (!(other instanceof SemanticVersion)) {
      return myOriginalString.compareTo(other.toString());
    }

    SemanticVersion o = (SemanticVersion)other;
    int result = myVersion.compareTo(o.myVersion);
    if (result != 0) return result;
    boolean empty = StringUtil.isEmpty(myRelease);
    boolean otherEmpty = StringUtil.isEmpty(o.myRelease);
    if (empty && otherEmpty) return 0;
    else if (empty) return 1;
    else if (otherEmpty) return -1;

    String[] o1 = split(myRelease);
    String[] o2 = split(o.myRelease);

    int x;
    for(int i = 0, max = Math.min(o1.length, o2.length); i < max; i++) {
      if ((x = compareElements(o1[i], o2[i]))!= 0) return x;
    }
    if (o1.length == 0 && o2.length > 0) return 1;
    if (o2.length == 0 && o1.length > 0) return -1;

    if (o1.length < o2.length) return -1;
    if (o1.length > o2.length) return 1;

    return 0;
  }

  public boolean equals(SemanticVersion that){
    if (myRelease != null ? !myRelease.equals(that.myRelease) : that.myRelease != null) return false;
    return myVersion.equals(that.myVersion);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return equals((SemanticVersion) o);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 43 * hash + this.myVersion.hashCode();
    hash = 43 * hash + (this.myRelease != null ? this.myRelease.hashCode() : 0);
    hash = 43 * hash + (this.myMetadata != null ? this.myMetadata.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString()
  {
    return myOriginalString;
  }

  private int compareElements(@NotNull String s1, @NotNull String s2) {
    int i1 = 0;
    int i2 = 0;
    boolean isInt1 = true;
    boolean isInt2 = true;
    try {
      i1 = Integer.parseInt(s1);
    } catch (Exception e) {
      isInt1 = false;
    }
    try {
      i2 = Integer.parseInt(s2);
    } catch (Exception e) {
      isInt2 = false;
    }

    if (isInt1 && isInt2) {
      if (i1 == i2) return 0;
      if (i1 < i2) return -1;
      if (i1 > i2) return 1;
    }

    if (isInt1 && !isInt2) {
      return -1;
    }

    if (!isInt1 && isInt2) {
      return 1;
    }

    return s1.compareTo(s2);
  }

  @NotNull
  private String[] split(@Nullable String s) {
    if (s == null || s.length() == 0) return new String[0];
    return Pattern.compile("\\.").split(s);
  }

  @NotNull
  @Override
  public SemVerLevel getLevel() {
    return myLevel;
  }
}
