package jetbrains.buildServer.nuget.agent.util;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 15:36
 */
public interface CommandlineBuildProcessFactory {
  BuildProcess executeCommandLine(@NotNull BuildRunnerContext hostContext,
                                  @NotNull ProgramCommandLine cmd) throws RunBuildException;
}
