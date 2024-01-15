

package jetbrains.buildServer.nuget.tests.util;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.tests.LoggingTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 20:08
 */
public class BuildProcessTestCase extends LoggingTestCase {

  protected static <T> T[] t(T...t) {
    return t;
  }

  protected static <T> Collection<T> c(T...t) {
    return Arrays.asList(t);
  }

  @DataProvider(name = "buildFinishStatuses")
  public Object[][] buildStatuses() {
    List<Object[]> list = new ArrayList<Object[]>();
    for (BuildFinishedStatus val : BuildFinishedStatus.values()) {
      list.add(new Object[]{val});
    }
    return list.toArray(new Object[list.size()][]);
  }

  protected void assertRunSuccessfully(@NotNull BuildProcess proc, @Nullable BuildFinishedStatus result) {
    BuildFinishedStatus status = null;
    try {
      proc.start();
      status = proc.waitFor();
    } catch (RunBuildException e) {
      Assert.fail("Failed with exception " + e);
      e.printStackTrace(System.err);
    }

    Assert.assertNotNull(status);
    if (result != null) {
      Assert.assertEquals(status, result);
    }
  }

  protected void assertRunException(@NotNull BuildProcess proc, @NotNull String message) {
    try {
      proc.start();
      proc.waitFor();
      Assert.fail("Exception expected");
    } catch (RunBuildException e) {
      Assert.assertTrue(e.getMessage().contains(message), e.toString());
    }
  }

  /**
  * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
  * Date: 07.07.11 20:07
  */
  protected class RecordingBuildProcess implements BuildProcess {
    private final String myId;
    private final BuildFinishedStatus myResultStatus;
    private Throwable myStartException;
    private Throwable myFinishException;

    RecordingBuildProcess(@NotNull String id,
                          @Nullable final BuildFinishedStatus resultStatus) {
      myId = id;
      myResultStatus = resultStatus;
    }

    public void setStartException(Exception startException) {
      myStartException = startException;
    }

    public void setFinishException(Exception finishException) {
      myFinishException = finishException;
    }

    public void start() throws RunBuildException {
      log("start-" + myId);
      throwExceptionIfPossible(myStartException);
    }

    private void throwExceptionIfPossible(Throwable ex) throws RunBuildException {
      if (ex != null) {
        if (ex instanceof RunBuildException) throw (RunBuildException) ex;
        if (ex instanceof RuntimeException) throw (RuntimeException) ex;
        throw (Error) ex;
      }
    }

    public boolean isInterrupted() {
      return false;
    }

    public boolean isFinished() {
      return false;
    }

    public void interrupt() {
      log("interrupt-" + myId);
    }

    @NotNull
    public BuildFinishedStatus waitFor() throws RunBuildException {
      log("waitFor-" + myId);
      throwExceptionIfPossible(myFinishException);
      return myResultStatus;
    }
  }
}
