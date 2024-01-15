

package jetbrains.buildServer.nuget.agent.util;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 18:41
 */
public class DelegatingBuildProcess extends BuildProcessBase {
  private final AtomicReference<BuildProcess> myReference = new AtomicReference<BuildProcess>();
  private final Action myAction;

  public DelegatingBuildProcess(@NotNull final Action action) {
    myAction = action;
  }

  @Override
  protected final void interruptImpl() {
    super.interruptImpl();
    BuildProcess process = myReference.get();
    if (process != null) process.interrupt();
  }

  @NotNull
  @Override
  protected final BuildFinishedStatus waitForImpl() throws RunBuildException {
    try {
      BuildProcess process = myAction.startImpl();
      myReference.set(process);

      if (isInterrupted()) return BuildFinishedStatus.INTERRUPTED;
      process.start();
      return process.waitFor();
    } finally {
      myReference.set(null);
      myAction.finishedImpl();
    }
  }

  public static interface Action {
    @NotNull
    BuildProcess startImpl() throws RunBuildException;
    void finishedImpl();
  }
}
