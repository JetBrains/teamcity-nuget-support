package jetbrains.buildServer.nuget.common;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 22:55
 */
public class NuGetPackageInfo implements Comparable<NuGetPackageInfo> {
  private final String myId;
  private final String myVersion;

  public NuGetPackageInfo(@NotNull final String id,
                          @NotNull final String version) {
    myId = id;
    myVersion = version;
  }

  public int compareTo(NuGetPackageInfo o) {
    int x;
    if ((x = getId().compareTo(o.getId())) != 0) return x;
    if ((x = getVersion().compareTo(o.getVersion())) != 0) return x;
    return 0;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @NotNull
  public String getVersion() {
    return myVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NuGetPackageInfo that = (NuGetPackageInfo) o;
    return myId.equals(that.myId) && myVersion.equals(that.myVersion);
  }

  @Override
  public int hashCode() {
    int result = myId.hashCode();
    result = 31 * result + myVersion.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "NuGetPackageInfo{" +
            "myId='" + myId + '\'' +
            ", myVersion='" + myVersion + '\'' +
            '}';
  }
}
