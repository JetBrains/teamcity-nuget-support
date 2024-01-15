

package jetbrains.buildServer.nuget.agent.dependencies;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 23:50
 */
public interface PackageUsages {
  void reportInstalledPackages(@NotNull File packagesConfig);
  void reportCreatedPackages(@NotNull Collection<File> packageFiles);
  void reportPublishedPackage(@NotNull final File packageFile, @Nullable String source);
}
