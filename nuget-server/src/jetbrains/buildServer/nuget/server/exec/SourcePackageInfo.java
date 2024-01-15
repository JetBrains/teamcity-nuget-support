

package jetbrains.buildServer.nuget.server.exec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:17
 */
public class SourcePackageInfo {
  private final String mySource;
  private final String myPackageId;
  private final String myVersion;


  public SourcePackageInfo(@Nullable final String source,
                           @NotNull final String packageId,
                           @NotNull final String version) {
    mySource = source;
    myPackageId = packageId;
    myVersion = version;
  }

  @Nullable
  public String getSource() {
    return mySource;
  }

  @NotNull
  public String getPackageId() {
    return myPackageId;
  }

  @NotNull
  public String getVersion() {
    return myVersion;
  }

  @Override
  public String toString() {
    return "SourcePackageInfo{" +
            "mySource='" + mySource + '\'' +
            ", myPackageId='" + myPackageId + '\'' +
            ", myVersion='" + myVersion + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SourcePackageInfo that = (SourcePackageInfo) o;

    if (!myPackageId.equals(that.myPackageId)) return false;
    if (mySource != null ? !mySource.equals(that.mySource) : that.mySource != null) return false;
    if (!myVersion.equals(that.myVersion)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = mySource != null ? mySource.hashCode() : 0;
    result = 31 * result + myPackageId.hashCode();
    result = 31 * result + myVersion.hashCode();
    return result;
  }
}
