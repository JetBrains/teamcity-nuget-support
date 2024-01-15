

package jetbrains.buildServer.nuget.feed.server.index.impl.latest;

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import org.jetbrains.annotations.NotNull;

/**
* Created 19.03.13 14:37
*
* @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
*/
class IsAbsoluteLatestCalculator extends BaseLatestCalculator {
  @Override
  protected void updatePackageVersion(@NotNull NuGetPackageBuilder builder) {
    builder.setIsAbsoluteLatest(true);
  }
}
