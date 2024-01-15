

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackageTransformation;
import org.jetbrains.annotations.NotNull;

/**
* Created 19.06.13 15:31
*
* @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
*/
public class MockExternalIdTransformation implements PackageTransformation {
  @NotNull
  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    final String id = builder.getBuildTypeId();
    if (id == null) return Status.SKIP;
    builder.setBuildTypeExternalId(id);
    return Status.CONTINUE;
  }

  @NotNull
  public PackageTransformation createCopy() {
    return this;
  }
}
