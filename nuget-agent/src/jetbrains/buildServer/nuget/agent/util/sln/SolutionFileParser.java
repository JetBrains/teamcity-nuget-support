

package jetbrains.buildServer.nuget.agent.util.sln;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 20:04
 */
public interface SolutionFileParser {
  /**
   * Parses .sln file to fetch project files from it.
   * Web projects does not have explicit project files, thus,
   * web project home directory will be returned.
   *
   *
   * @param logger logger to log parse progress
   * @param sln path to solution file
   * @return collection of full paths to referenced projects files or home directories
   */
  @NotNull
  Collection<File> parseProjectFiles(@NotNull final BuildProgressLogger logger,
                                     @NotNull final File sln) throws RunBuildException;
}
