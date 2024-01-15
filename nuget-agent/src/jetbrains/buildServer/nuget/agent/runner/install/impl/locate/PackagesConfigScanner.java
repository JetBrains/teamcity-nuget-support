

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 14:02
 */
public interface PackagesConfigScanner {
  @NotNull
  Collection<File> scanResourceConfig(@NotNull BuildProgressLogger logger,
                                      @NotNull final File sln,
                                      @NotNull final File packages) throws RunBuildException;
}
