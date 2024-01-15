

package jetbrains.buildServer.nuget.feed.server.index.impl.latest;

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import org.jetbrains.annotations.NotNull;

/**
* Created 19.03.13 14:37
*
* @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
*/
public class IsLatestCalculator extends BaseLatestCalculator {
  @Override
  public void updatePackage(@NotNull NuGetPackageBuilder newLatest) {
    if (newLatest.isPrerelease()) return;
    super.updatePackage(newLatest);
  }

  @Override
  protected void updatePackageVersion(@NotNull NuGetPackageBuilder builder) {
    builder.setIsLatest(true);
  }
}
