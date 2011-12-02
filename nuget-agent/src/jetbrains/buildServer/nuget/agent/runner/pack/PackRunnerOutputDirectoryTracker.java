package jetbrains.buildServer.nuget.agent.runner.pack;

import jetbrains.buildServer.agent.AgentRunningBuild;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.12.11 15:07
 */
public interface PackRunnerOutputDirectoryTracker {
  @NotNull
  PackRunnerOutputDirectoryTrackerImpl.TrackState getState(@NotNull AgentRunningBuild build);

  void removeTrackState(@NotNull AgentRunningBuild build);
}
