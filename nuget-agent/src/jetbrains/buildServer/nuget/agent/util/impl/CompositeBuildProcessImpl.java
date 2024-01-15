

package jetbrains.buildServer.nuget.agent.util.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CompositeBuildProcess;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 14:04
 */
public class CompositeBuildProcessImpl extends BuildProcessBase implements CompositeBuildProcess {
  private final BlockingQueue<BuildProcess> myProcessList = new LinkedBlockingQueue<BuildProcess>();
  private final AtomicReference<BuildProcess> myCurrentProcess = new AtomicReference<BuildProcess>();

  public void pushBuildProcess(@NotNull final BuildProcess process) {
    myProcessList.add(process);
  }

  @Override
  protected void interruptImpl() {
    BuildProcess process = myCurrentProcess.get();
    if (process != null) {
      process.interrupt();
    }
  }

  public void start() throws RunBuildException {
  }

  @NotNull
  protected BuildFinishedStatus waitForImpl() throws RunBuildException {
    if (isInterrupted()) return BuildFinishedStatus.INTERRUPTED;
    for (BuildProcess proc = myProcessList.poll(); proc != null; proc = myProcessList.poll()) {
      myCurrentProcess.set(proc);
      try {
        proc.start();
        final BuildFinishedStatus status = proc.waitFor();
        if (status != BuildFinishedStatus.INTERRUPTED && status != BuildFinishedStatus.FINISHED_SUCCESS) return status;
      } finally {
        myCurrentProcess.set(null);
      }
      if (isInterrupted()) return BuildFinishedStatus.INTERRUPTED;
    }
    if (isInterrupted()) return BuildFinishedStatus.INTERRUPTED;
    return BuildFinishedStatus.FINISHED_SUCCESS;
  }
}
