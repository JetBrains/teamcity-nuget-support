

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 15:49
 */
public class SolutionWidePackagesConfigScanner implements PackagesConfigScanner {

  @NotNull
  public Collection<File> scanResourceConfig(@NotNull BuildProgressLogger logger, @NotNull File sln, @NotNull File packages) throws RunBuildException {
    final File solutionPackagesConfig = findSolutionPackagesConfigFile(sln);
    if (solutionPackagesConfig != null) {
      logger.message("Found solution-wide packages.config: " + solutionPackagesConfig);
      return Collections.singleton(solutionPackagesConfig);
    }

    return Collections.emptyList();
  }

  @Nullable
  private File findSolutionPackagesConfigFile(@NotNull final File sln) {
    final File parentFile = sln.getParentFile();
    if (parentFile == null) return null;

    final File path = new File(parentFile, ".nuget/packages.config");

    if (path.isFile()) return path;
    return null;
  }

}
