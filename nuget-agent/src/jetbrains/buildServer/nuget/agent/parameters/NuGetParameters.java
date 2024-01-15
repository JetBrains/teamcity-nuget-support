

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.RunBuildException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 15:22
 */
public interface NuGetParameters {
  /**
   * @return path to nuget.exe file
   * @throws jetbrains.buildServer.RunBuildException if nuget was not found
   */
  @NotNull
  File getNuGetExeFile() throws RunBuildException;
}
