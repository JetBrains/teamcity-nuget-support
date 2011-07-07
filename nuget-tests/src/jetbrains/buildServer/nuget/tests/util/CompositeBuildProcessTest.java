/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.tests.util;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.util.StringUtil;
import org.testng.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 19:04
 */
public class CompositeBuildProcessTest extends BaseTestCase {
  @Test
  public void test_empty_build_process() {
    CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    assertRunSuccessfully(i, BuildFinishedStatus.FINISHED_SUCCESS);
  }

  @Test(dataProvider = "buildFinishStatuses")
  public void test_one_build_process(BuildFinishedStatus result) throws RunBuildException {
    CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    final List<String> log = new ArrayList<String>();
    i.pushBuildProcess(new RecordingBuildProcess("1", result, log));
    assertRunSuccessfully(i, result == BuildFinishedStatus.INTERRUPTED ? BuildFinishedStatus.FINISHED_SUCCESS : result);

    assertLog(log, "start-1", "waitFor-1");
  }

  @Test
  public void test_stopOnFirstError() throws RunBuildException {
    CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    final List<String> log = new ArrayList<String>();
    i.pushBuildProcess(new RecordingBuildProcess("1", BuildFinishedStatus.FINISHED_FAILED, log));
    i.pushBuildProcess(new RecordingBuildProcess("2", BuildFinishedStatus.FINISHED_SUCCESS, log));

    assertRunSuccessfully(i, BuildFinishedStatus.FINISHED_FAILED);

    assertLog(log, "start-1", "waitFor-1");
  }

  @Test
  public void test_stopOnFirstError2() throws RunBuildException {
    CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    final List<String> log = new ArrayList<String>();
    i.pushBuildProcess(new RecordingBuildProcess("1", BuildFinishedStatus.FINISHED_SUCCESS, log));
    i.pushBuildProcess(new RecordingBuildProcess("2", BuildFinishedStatus.FINISHED_FAILED, log));
    i.pushBuildProcess(new RecordingBuildProcess("3", BuildFinishedStatus.FINISHED_SUCCESS, log));

    assertRunSuccessfully(i, BuildFinishedStatus.FINISHED_FAILED);
    assertLog(log, "start-1", "waitFor-1", "start-2", "waitFor-2");
  }

  @Test
  public void test_stopOnStartException() throws RunBuildException {
    CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    final List<String> log = new ArrayList<String>();
    i.pushBuildProcess(new RecordingBuildProcess("1", BuildFinishedStatus.FINISHED_SUCCESS, log));
    i.pushBuildProcess(new RecordingBuildProcess("2", BuildFinishedStatus.FINISHED_SUCCESS, log) {{
      setStartException(new RunBuildException("aaa"));
    }});
    i.pushBuildProcess(new RecordingBuildProcess("3", BuildFinishedStatus.FINISHED_SUCCESS, log));

    assertRunException(i, "aaa");
    assertLog(log, "start-1", "waitFor-1", "start-2");
  }

  @Test
  public void test_stopOnWaitForException() throws RunBuildException {
    CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    final List<String> log = new ArrayList<String>();
    i.pushBuildProcess(new RecordingBuildProcess("1", BuildFinishedStatus.FINISHED_SUCCESS, log));
    i.pushBuildProcess(new RecordingBuildProcess("2", BuildFinishedStatus.FINISHED_SUCCESS, log) {{
      setFinishException(new RunBuildException("aaa"));
    }});
    i.pushBuildProcess(new RecordingBuildProcess("3", BuildFinishedStatus.FINISHED_SUCCESS, log));

    assertRunException(i, "aaa");
    assertLog(log, "start-1", "waitFor-1", "start-2", "waitFor-2");
  }

  @Test
  public void test_emptyInterrupted() {
    CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    i.interrupt();

    Assert.assertFalse(i.isFinished());
    Assert.assertTrue(i.isInterrupted());
    assertRunSuccessfully(i, BuildFinishedStatus.INTERRUPTED);

    Assert.assertTrue(i.isInterrupted());
    Assert.assertTrue(i.isFinished());
  }

  @Test
  public void test_interruptCalledForFirst() {
    final CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    final List<String> log = new ArrayList<String>();
    i.pushBuildProcess(new RecordingBuildProcess("1",BuildFinishedStatus.FINISHED_SUCCESS, log) {
      @Override
      public void start() throws RunBuildException {
        super.start();
        i.interrupt();
      }
    });
    i.pushBuildProcess(new RecordingBuildProcess("f", BuildFinishedStatus.FINISHED_SUCCESS, log));

    assertRunSuccessfully(i, BuildFinishedStatus.INTERRUPTED);
    assertLog(log, "start-1", "interrupt-1", "waitFor-1");
  }

  @Test
  public void test_interruptCalledForFirst_WaitFor() {
    final CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    final List<String> log = new ArrayList<String>();
    i.pushBuildProcess(new RecordingBuildProcess("1",BuildFinishedStatus.FINISHED_SUCCESS, log) {
      @NotNull
      @Override
      public BuildFinishedStatus waitFor() throws RunBuildException {
        i.interrupt();
        return super.waitFor();
      }
    });
    i.pushBuildProcess(new RecordingBuildProcess("f", BuildFinishedStatus.FINISHED_SUCCESS, log));

    assertRunSuccessfully(i, BuildFinishedStatus.INTERRUPTED);
    assertLog(log, "start-1", "interrupt-1", "waitFor-1");
  }

  @Test
  public void test_interruptCalledForTwo() {
    final CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    final List<String> log = new ArrayList<String>();
    i.pushBuildProcess(new RecordingBuildProcess("0", BuildFinishedStatus.FINISHED_SUCCESS, log));
    i.pushBuildProcess(new RecordingBuildProcess("1",BuildFinishedStatus.FINISHED_SUCCESS, log) {
      @Override
      public void start() throws RunBuildException {
        super.start();
        i.interrupt();
      }
    });
    i.pushBuildProcess(new RecordingBuildProcess("f", BuildFinishedStatus.FINISHED_SUCCESS, log));

    assertRunSuccessfully(i, BuildFinishedStatus.INTERRUPTED);
    assertLog(log, "start-0", "waitFor-0", "start-1", "interrupt-1", "waitFor-1");
  }

  @Test
  public void test_interruptCalledForTwo_WaitFor() {
    final CompositeBuildProcessImpl i = new CompositeBuildProcessImpl();
    final List<String> log = new ArrayList<String>();
    i.pushBuildProcess(new RecordingBuildProcess("0", BuildFinishedStatus.FINISHED_SUCCESS, log));
    i.pushBuildProcess(new RecordingBuildProcess("1",BuildFinishedStatus.FINISHED_SUCCESS, log) {
      @NotNull
      @Override
      public BuildFinishedStatus waitFor() throws RunBuildException {
        i.interrupt();
        return super.waitFor();
      }
    });
    i.pushBuildProcess(new RecordingBuildProcess("f", BuildFinishedStatus.FINISHED_SUCCESS, log));

    assertRunSuccessfully(i, BuildFinishedStatus.INTERRUPTED);
    assertLog(log, "start-0", "waitFor-0", "start-1", "interrupt-1", "waitFor-1");
  }



  @DataProvider(name = "buildFinishStatuses")
  public Object[][] buildStatuses() {
    List<Object[]> list = new ArrayList<Object[]>();
    for (BuildFinishedStatus val : BuildFinishedStatus.values()) {
      list.add(new Object[]{val});
    }
    return list.toArray(new Object[list.size()][]);
  }

  private void assertRunSuccessfully(@NotNull BuildProcess proc, @NotNull BuildFinishedStatus result) {
    BuildFinishedStatus status = null;
    try {
      proc.start();
      status = proc.waitFor();
    } catch (RunBuildException e) {
      Assert.fail("Failed with exception " + e);
    }

    Assert.assertEquals(result, status);
  }

  private void assertRunException(@NotNull BuildProcess proc, @NotNull String message) {
    try {
      proc.start();
      proc.waitFor();
      Assert.fail("Exception expected");
    } catch (RunBuildException e) {
      Assert.assertEquals(message, e.getMessage());
    }
  }

  private void assertLog(Collection<String> log, String... gold) {
    String actual = StringUtil.join(log, "\n");
    String expected = StringUtil.join(gold, "\n");
    Assert.assertEquals(actual, expected);
  }

  private class RecordingBuildProcess implements BuildProcess {
    private final String myId;
    private final List<String> myLog;
    private final BuildFinishedStatus myResultStatus;
    private Throwable myStartException;
    private Throwable myFinishException;

    private RecordingBuildProcess(@NotNull String id,
                                  @Nullable final BuildFinishedStatus resultStatus,
                                  @NotNull final List<String> log) {
      myId = id;
      myLog = log;
      myResultStatus = resultStatus;
    }

    public void setStartException(Exception startException) {
      myStartException = startException;
    }

    public void setFinishException(Exception finishException) {
      myFinishException = finishException;
    }

    public void start() throws RunBuildException {
      myLog.add("start-" + myId);
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
      myLog.add("interrupt-" + myId);
    }

    @NotNull
    public BuildFinishedStatus waitFor() throws RunBuildException {
      myLog.add("waitFor-" + myId);
      throwExceptionIfPossible(myFinishException);
      return myResultStatus;
    }
  }
}
