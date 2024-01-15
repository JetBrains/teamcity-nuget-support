

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 16.11.11 14:32
 */
public class PackagesHashCalculator {
  private static final String ourVersion = "v2";

  @NotNull
  public String serializeHashcode(@NotNull final Collection<SourcePackageInfo> _packages) {
    List<SourcePackageInfo> sorted = new ArrayList<SourcePackageInfo>(_packages);
    Collections.sort(sorted, new Comparator<SourcePackageInfo>() {
      public int compare(SourcePackageInfo o1, SourcePackageInfo o2) {
        int i;
        String s1 = o1.getSource();
        String s2 = o2.getSource();
        if (s1 == null && s2 != null) return 1;
        if (s1 != null && s2 == null) return -1;

        //noinspection ConstantConditions
        if (s1 != null && s2 != null && 0 != (i = s1.compareTo(s2))) return i;
        if (0 != (i = o1.getPackageId().compareTo(o2.getPackageId()))) return i;
        if (0 != (i = o1.getVersion().compareTo(o2.getVersion()))) return i;
        return 0;
      }
    });

    final StringBuilder sb = new StringBuilder();
    sb.append(ourVersion);
    for (SourcePackageInfo info : sorted) {
      String source = info.getSource();
      if (source != null) {
        sb.append("|s:").append(source);
      }
      sb.append("|p:").append(info.getPackageId());
      sb.append("|v:").append(info.getVersion());
    }
    return sb.toString();
  }

  public boolean isUpgradeRequired(@Nullable final String oldValue, @NotNull final String newValue) {
    return oldValue != null && !newValue.equals(oldValue) && oldValue.startsWith("v2");
  }
}
