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

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.nuget.server.trigger.BuildStartReason;
import jetbrains.buildServer.nuget.server.trigger.ThreadedBuildTriggerPolicy;
import jetbrains.buildServer.nuget.server.trigger.TriggerUpdateChecker;
import jetbrains.buildServer.serverSide.SBuildType;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.07.11 14:10
 */
public class ThreadedBuildTriggerPolicyTest extends BaseTestCase {
  private Mockery m;
  private ExecutorService executor;
  private TriggerUpdateChecker checker;
  private ThreadedBuildTriggerPolicy policy;
  private PolledTriggerContext ctx;
  private Future future;
  private SBuildType bt;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    executor = m.mock(ExecutorService.class);
    checker = m.mock(TriggerUpdateChecker.class);
    policy = new ThreadedBuildTriggerPolicy(executor, checker);
    ctx = m.mock(PolledTriggerContext.class);
    future = m.mock(Future.class);
    bt = m.mock(SBuildType.class);

    m.checking(new Expectations(){{
      allowing(ctx).getBuildType(); will(returnValue(bt));
    }});
  }

  @Test
  public void test_should_start_thread_on_call() {
    m.checking(new Expectations(){{
      //noinspection unchecked
      oneOf(executor).submit(with(any(Callable.class)));
      will(returnValue(future));

      oneOf(future).isDone(); will(returnValue(false));
    }});
     policy.triggerBuild(ctx);
  }

  @Test
  public void test_should_not_restart_thread_on_second_call() {
    test_should_start_thread_on_call();

    m.checking(new Expectations(){{
      oneOf(future).isDone(); will(returnValue(false));
    }});

    policy.triggerBuild(ctx);
  }

  @Test
  public void test_should_start_build_on_result() throws ExecutionException, InterruptedException {
    test_should_start_thread_on_call();

    m.checking(new Expectations(){{
      oneOf(future).isDone(); will(returnValue(true));
      oneOf(future).get(); will(returnValue(new BuildStartReason("aaa")));
      oneOf(bt).addToQueue("aaa");
    }});

    policy.triggerBuild(ctx);
  }

  @Test
  public void test_should_not_start_build_on_result() throws ExecutionException, InterruptedException {
    test_should_start_thread_on_call();

    m.checking(new Expectations(){{
      oneOf(future).isDone(); will(returnValue(true));
      oneOf(future).get(); will(returnValue(null));
    }});

    policy.triggerBuild(ctx);
  }

  @Test
  public void test_should_throw_on_execution_exception() throws ExecutionException, InterruptedException {
    test_should_start_thread_on_call();

    m.checking(new Expectations(){{
      oneOf(future).isDone(); will(returnValue(true));
      oneOf(future).get(); will(throwException(new ExecutionException(new RuntimeException("fail"))));
    }});

    try {
      policy.triggerBuild(ctx);
      Assert.fail("excption must be thrown");
    } catch (BuildTriggerException e) {
      Assert.assertEquals(e.getMessage(), "fail");
    }
  }

  @Test
  public void test_should_not_fail_on_rejected_execution_exception() {
    m.checking(new Expectations(){{
      //noinspection unchecked
      oneOf(executor).submit(with(any(Callable.class)));
      will(throwException(new RejectedExecutionException("failed to exec")));
    }});

    policy.triggerBuild(ctx);
  }
}
