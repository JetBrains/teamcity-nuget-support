

package jetbrains.buildServer.nuget.feed.server.index.impl.latest;

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Created 19.03.13 14:32
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class LatestVersionsCalculator implements LatestCalculator {
  private final LatestCalculator[] myCalcs = {
          new IsLatestCalculator(),
          new IsAbsoluteLatestCalculator()};

  public void updatePackage(@NotNull NuGetPackageBuilder newLatest) {
    for (LatestCalculator calc : myCalcs) {
      calc.updatePackage(newLatest);
    }
  }

  public void updateSelectedPackages() {
    for (LatestCalculator calc : myCalcs) {
      calc.updateSelectedPackages();
    }
  }
}
