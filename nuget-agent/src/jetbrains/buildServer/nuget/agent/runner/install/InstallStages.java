

package jetbrains.buildServer.nuget.agent.runner.install;

import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 23:36
 */
public interface InstallStages {
  @NotNull
  BuildProcessContinuation getLocateStage();

  @NotNull
  BuildProcessContinuation getInstallStage();

  @NotNull
  BuildProcessContinuation getUpdateStage();

  @NotNull
  BuildProcessContinuation getPostUpdateStart();

  @NotNull
  BuildProcessContinuation getReportStage();
}
