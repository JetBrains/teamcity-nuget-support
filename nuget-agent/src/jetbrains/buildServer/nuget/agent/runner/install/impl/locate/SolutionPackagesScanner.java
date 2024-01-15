

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.util.sln.SolutionFileParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 14:48
 */
public class SolutionPackagesScanner implements PackagesConfigScanner {
  private final SolutionFileParser myParser;

  public SolutionPackagesScanner(@NotNull final SolutionFileParser parser) {
    myParser = parser;
  }

  @NotNull
  public Collection<File> scanResourceConfig(@NotNull final BuildProgressLogger logger,
                                             @NotNull final File sln,
                                             @NotNull final File packages) throws RunBuildException {
    final Collection<File> result = new ArrayList<File>();
    final Collection<File> files = myParser.parseProjectFiles(logger, sln);
    logger.message("Scanning projects in solution file: " + sln);
    for (File file : files) {
      logger.message("Found project " + file.getPath());
      final File config;
      if (file.isFile()) {
        File parentFile = file.getParentFile();
        if (parentFile == null){
          config = null;
        } else {
          config = new File(parentFile, "packages.config");
        }
      } else {
        config = new File(file, "packages.config");
      }

      if (config != null && config.isFile()) {
        result.add(config);
      }
    }

    return result;
  }
}
