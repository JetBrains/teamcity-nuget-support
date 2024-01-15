

package jetbrains.buildServer.nuget.feed.server.index.impl;

import org.jetbrains.annotations.NotNull;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:17
*/
public interface PackageTransformation {
  @NotNull
  Status applyTransformation(@NotNull final NuGetPackageBuilder builder);

  @NotNull
  PackageTransformation createCopy();

  enum Status {
    SKIP, CONTINUE
  }
}
