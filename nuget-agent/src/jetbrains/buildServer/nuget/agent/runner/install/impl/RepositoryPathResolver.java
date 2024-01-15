

package jetbrains.buildServer.nuget.agent.runner.install.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.12.11 18:03
 */
public interface RepositoryPathResolver {
  @NotNull
  File resolveRepositoryPath(@NotNull BuildProgressLogger logger,
                             @NotNull File solutionFile,
                             @NotNull File workingDirectory) throws RunBuildException;
}
