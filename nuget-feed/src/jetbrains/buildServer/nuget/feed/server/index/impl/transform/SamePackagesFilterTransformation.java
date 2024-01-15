

package jetbrains.buildServer.nuget.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackageTransformation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:29
*/
public class SamePackagesFilterTransformation implements PackageTransformation {
  private final Set<String> reportedPackages = new HashSet<String>();

  @NotNull
  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    return reportedPackages.add(builder.getKey()) ? Status.CONTINUE : Status.SKIP;
  }

  @NotNull
  public PackageTransformation createCopy() {
    return new SamePackagesFilterTransformation();
  }
}
