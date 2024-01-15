

package jetbrains.buildServer.nuget.agent.util.impl;

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner2.GenericCommandLineBuildProcess;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 15:13
 */
public class NuGetCommandBuildProcessFactory implements CommandlineBuildProcessFactory {
  private final ExtensionHolder myExtensionHolder;
  private final NuGetCommandLineProvider myProvider;

  public NuGetCommandBuildProcessFactory(@NotNull final ExtensionHolder extensionHolder,
                                         @NotNull final NuGetCommandLineProvider commandLineProvider) {
    myExtensionHolder = extensionHolder;
    myProvider = commandLineProvider;
  }

  @NotNull
  public BuildProcess executeCommandLine(@NotNull final BuildRunnerContext context,
                                         @NotNull final String executable,
                                         @NotNull final Collection<String> arguments,
                                         @NotNull final File workingDir,
                                         @NotNull final Map<String, String> env) {
    final ProgramCommandLine programCommandLine = myProvider.getProgramCommandLine(
      context, executable, arguments, workingDir, env
    );
    final CommandLineBuildSession commandLineBuildSession = new CommandLineBuildSession(programCommandLine, context);
    return new GenericCommandLineBuildProcess(context, commandLineBuildSession, myExtensionHolder);
  }
}
