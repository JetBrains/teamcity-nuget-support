

package jetbrains.buildServer.nuget.feed.server.index.impl.latest;

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 19.03.13 14:31
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public abstract class BaseLatestCalculator implements LatestCalculator {
  private final Map<String, NuGetPackageBuilder> myLatestPackages = new HashMap<String, NuGetPackageBuilder>();

  public void updatePackage(@NotNull NuGetPackageBuilder newLatest) {
    final NuGetPackageBuilder currentLatest = myLatestPackages.get(newLatest.getPackageName());

    if (currentLatest == null || currentLatest.getVersion().compareTo(newLatest.getVersion()) < 0) {
      myLatestPackages.put(newLatest.getPackageName(), newLatest);
    }
  }

  public void updateSelectedPackages() {
    for (NuGetPackageBuilder builder : myLatestPackages.values()) {
      updatePackageVersion(builder);
    }
    myLatestPackages.clear();
  }

  protected abstract void updatePackageVersion(@NotNull NuGetPackageBuilder builder);
}
