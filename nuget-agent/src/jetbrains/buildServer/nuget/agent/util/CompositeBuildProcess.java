package jetbrains.buildServer.nuget.agent.util;

import jetbrains.buildServer.agent.BuildProcess;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 15:37
 */
public interface CompositeBuildProcess extends BuildProcess {
  void pushBuildProcess(@NotNull BuildProcess process);
}
