package jetbrains.buildServer.nuget.agent.util.impl;

import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Executes command line and logs output.
 */
public class CommandLineBuildSession extends ProcessListenerAdapter implements MultiCommandBuildSession, CommandExecution {

  private final ProgramCommandLine myCommandLine;
  private final BuildRunnerContext myHostContext;
  private boolean myExecuted;
  private int myExitCode;

  CommandLineBuildSession(@NotNull ProgramCommandLine commandLine,
                          @NotNull BuildRunnerContext hostContext) {
    myCommandLine = commandLine;
    myHostContext = hostContext;
  }

  @Override
  public void sessionStarted() {
  }

  @Nullable
  @Override
  public CommandExecution getNextCommand() {
    if (!myExecuted) {
      myExecuted = true;
      return this;
    }
    return null;
  }

  @NotNull
  @Override
  public ProgramCommandLine makeProgramCommandLine() {
    return myCommandLine;
  }

  @Override
  public void beforeProcessStarted() {
  }

  @Override
  public void onStandardOutput(@NotNull String text) {
    if (text.startsWith("WARNING")) {
      myHostContext.getBuild().getBuildLogger().warning(text);
    } else {
      myHostContext.getBuild().getBuildLogger().message(text);
    }
  }

  @Override
  public void onErrorOutput(@NotNull String text) {
    myHostContext.getBuild().getBuildLogger().error(text);
  }

  @Override
  public void processFinished(int exitCode) {
    myExitCode = exitCode;
  }

  @Nullable
  @Override
  public BuildFinishedStatus sessionFinished() {
    return myExitCode == 0 ? BuildFinishedStatus.FINISHED_SUCCESS : BuildFinishedStatus.FINISHED_FAILED;
  }

  @NotNull
  @Override
  public TerminationAction interruptRequested() {
    return TerminationAction.KILL_PROCESS_TREE;
  }

  @Override
  public boolean isCommandLineLoggingEnabled() {
    return true;
  }
}
