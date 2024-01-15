

package jetbrains.buildServer.nuget.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 26.06.12 14:50
 */
public class SourcePackageInfo implements Comparable<SourcePackageInfo> {
  private final NuGetPackageInfo myPackageInfo;
  private final String mySource;

  public SourcePackageInfo(@NotNull final NuGetPackageInfo packageInfo,
                           @Nullable final String source) {
    myPackageInfo = packageInfo;
    mySource = source;
  }

  @NotNull
  public NuGetPackageInfo getPackageInfo() {
    return myPackageInfo;
  }

  @Nullable
  public String getSource() {
    return mySource;
  }

  public int compareTo(@NotNull final SourcePackageInfo o) {
    int x;
    if ((x = this.myPackageInfo.compareTo(o.myPackageInfo)) != 0) return x;
    if (this.mySource == null) return o.mySource == null ? 0 : 1;
    if (o.mySource == null) return -1;
    return this.mySource.compareToIgnoreCase(o.mySource);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SourcePackageInfo)) return false;

    SourcePackageInfo that = (SourcePackageInfo) o;

    if (!myPackageInfo.equals(that.myPackageInfo)) return false;
    if (mySource != null ? !mySource.equals(that.mySource) : that.mySource != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myPackageInfo.hashCode();
    result = 31 * result + (mySource != null ? mySource.hashCode() : 0);
    return result;
  }
}
