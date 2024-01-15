

package jetbrains.buildServer.nuget.feed.server.index.impl.latest;

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Created 19.03.13 13:17
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface LatestCalculator {
  void updatePackage(@NotNull NuGetPackageBuilder pkg);
  void updateSelectedPackages();
}
