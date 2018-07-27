package jetbrains.buildServer.nuget.common.version;

import org.jetbrains.annotations.NotNull;

/**
 * Non semantic package version.
 */
public class RegularVersion implements PackageVersion {

  private final String myVersion;

  public RegularVersion(@NotNull final String version) {
    myVersion = version;
  }

  @Override
  public int compareTo(@NotNull PackageVersion o) {
    return myVersion.compareTo(o.toString());
  }

  @Override
  public String toString() {
    return myVersion;
  }

  @NotNull
  @Override
  public SemVerLevel getLevel() {
    return SemVerLevel.NONE;
  }
}
