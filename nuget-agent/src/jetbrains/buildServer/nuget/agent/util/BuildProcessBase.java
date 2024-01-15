

package jetbrains.buildServer.nuget.agent.util;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 16:03
 */
public abstract class BuildProcessBase implements BuildProcess {
  private final AtomicBoolean myIsInterrupted = new AtomicBoolean();
  private final AtomicBoolean myIsFinished = new AtomicBoolean();

  public final boolean isInterrupted() {
    return myIsInterrupted.get();
  }

  public final boolean isFinished() {
    return myIsFinished.get();
  }

  public final void interrupt() {
    myIsInterrupted.set(true);
    interruptImpl();
  }

  @NotNull
  public final BuildFinishedStatus waitFor() throws RunBuildException {
    try {
      if (isInterrupted()) return BuildFinishedStatus.INTERRUPTED;
      BuildFinishedStatus status = waitForImpl();
      if (isInterrupted()) return BuildFinishedStatus.INTERRUPTED;

      return status;
    } finally {
      myIsFinished.set(true);
    }
  }

  @NotNull
  protected abstract BuildFinishedStatus waitForImpl() throws RunBuildException;

  protected void interruptImpl() {
  }

  public void start() throws RunBuildException {
  }
}
