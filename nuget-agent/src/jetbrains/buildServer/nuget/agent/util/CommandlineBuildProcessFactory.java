

package jetbrains.buildServer.nuget.agent.util;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 15:36
 */
public interface CommandlineBuildProcessFactory {
  @NotNull
  BuildProcess executeCommandLine(@NotNull BuildRunnerContext hostContext,
                                  @NotNull String program,
                                  @NotNull Collection<String> argz,
                                  @NotNull File workingDir,
                                  @NotNull Map<String, String> additionalEnvironment) throws RunBuildException;
}
