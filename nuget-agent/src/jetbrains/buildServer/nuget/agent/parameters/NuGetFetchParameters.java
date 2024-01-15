

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.RunBuildException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.07.11 14:27
 */
public interface NuGetFetchParameters extends NuGetParameters {

  /**
   * @return collection of nuget sources to fetch packages
   */
  @NotNull
  Collection<String> getNuGetPackageSources();

  /**
   * @return path to solution file.
   * @throws jetbrains.buildServer.RunBuildException
   *          if .sln file is not found
   */
  @NotNull
  File getSolutionFile() throws RunBuildException;

  /**
   * @return current working directory.
   */
  @NotNull
  File getWorkingDirectory();

  @NotNull
  Collection<String> getCustomCommandline();
}
