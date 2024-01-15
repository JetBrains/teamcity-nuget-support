

package jetbrains.buildServer.nuget.feed.server.index.impl;

import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Created 18.03.13 15:50
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class SemanticVersionsComparators {
  @NotNull
  public static Comparator<NuGetPackageBuilder> getBuildersComparator() {
    return (o1, o2) -> {
      final String id1 = o1.getPackageName();
      final String id2 = o2.getPackageName();

      int cmp;
      if ((cmp = id1.compareTo(id2)) != 0) return cmp;

      return o1.getVersion().compareTo(o2.getVersion());
    };
  }

  @NotNull
  public static Comparator<NuGetIndexEntry> getEntriesComparator() {
    return (o1, o2) -> {
      final String id1 = o1.getPackageInfo().getId();
      final String id2 = o2.getPackageInfo().getId();

      int cmp;
      if ((cmp = id1.compareTo(id2)) != 0) return cmp;

      return o1.getVersion().compareTo(o2.getVersion());
    };
  }
}
