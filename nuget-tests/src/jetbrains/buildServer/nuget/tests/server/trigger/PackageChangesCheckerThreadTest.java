/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.*;
import jetbrains.buildServer.nuget.server.trigger.impl.source.NuGetSourceChecker;
import jetbrains.buildServer.util.TimeService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 18:18
 */
public class PackageChangesCheckerThreadTest extends BaseTestCase {
  private PackageChangesCheckerThreadTask myTask;
  private Mockery m;
  private PackageChecker myChecker1;
  private PackageChecker myChecker2;
  private PackageCheckQueue myQueue;
  private ScheduledExecutorService myService;
  private TimeService myTime;
  private boolean myIsShutdown;
  private NuGetSourceChecker mySourceChecker;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myIsShutdown = false;
    myChecker1 = m.mock(PackageChecker.class, "checker1");
    myChecker2 = m.mock(PackageChecker.class, "checker2");
    myService = m.mock(ScheduledExecutorService.class);
    myQueue = m.mock(PackageCheckQueue.class);
    myTime = m.mock(TimeService.class);
    mySourceChecker = m.mock(NuGetSourceChecker.class);

    myTask = new PackageChangesCheckerThreadTask(myQueue, myService, Arrays.asList(myChecker1, myChecker2), mySourceChecker);

    m.checking(new Expectations(){{
      allowing(myTime).now(); will(returnValue(1L));
      allowing(myService).isShutdown(); will(new CustomAction("isShutdown flag") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myIsShutdown;
        }
      });

      allowing(myService).submit(with(any(Runnable.class))); will(new CustomAction("inplace execution") {
        public Object invoke(Invocation invocation) throws Throwable {
          Runnable action = (Runnable) invocation.getParameter(0);
          action.run();
          return null;
        }
      });
    }});
  }

  @Test
  public void test_no_work_if_shutdown() {
    myIsShutdown = true;
    myTask.checkForUpdates();

    m.assertIsSatisfied();
  }

  @Test
  public void test_schedule_empty() {
    m.checking(new Expectations(){{
      allowing(myQueue).getItemsToCheckNow(); will(returnValue(Collections.emptyList()));
    }});

    myTask.checkForUpdates();

    m.assertIsSatisfied();
  }

  @Test
  public void test_schecule_execution() {
    final PackageCheckEntry req = createEntry();

    m.checking(new Expectations(){{
      oneOf(myQueue).getItemsToCheckNow(); will(returnValue(Arrays.asList(req)));

      oneOf(myChecker1).accept(req.getRequest()); will(returnValue(true));
      oneOf(myChecker1).update(with(equal(myService)), with(equal(Arrays.<CheckablePackage>asList(req))));
      oneOf(myChecker2).accept(req.getRequest()); will(returnValue(false));

      oneOf(mySourceChecker).getAccessiblePackages(with(any(Collection.class)));
      will(returnSame());
    }});

    myTask.checkForUpdates();

    m.assertIsSatisfied();
  }

  @Test
  public void test_schecule_execution_filter() {
    final PackageCheckEntry req = createEntry();

    m.checking(new Expectations(){{
      oneOf(myQueue).getItemsToCheckNow(); will(returnValue(Arrays.asList(req)));

      oneOf(myChecker1).accept(req.getRequest()); will(returnValue(true));
      oneOf(myChecker2).accept(req.getRequest()); will(returnValue(false));

      oneOf(mySourceChecker).getAccessiblePackages(with(any(Collection.class)));
      will(returnValue(Collections.emptyList()));
    }});

    myTask.checkForUpdates();

    m.assertIsSatisfied();
  }

  @Test
  public void test_schecule_execution2() {
    final PackageCheckEntry req = createEntry();

    m.checking(new Expectations(){{
      oneOf(myQueue).getItemsToCheckNow(); will(returnValue(Arrays.asList(req)));

      oneOf(myChecker1).accept(req.getRequest()); will(returnValue(false));
      oneOf(myChecker2).accept(req.getRequest()); will(returnValue(true));
      oneOf(myChecker2).update(with(equal(myService)), with(equal(Arrays.<CheckablePackage>asList(req))));

      oneOf(mySourceChecker).getAccessiblePackages(with(any(Collection.class)));
      will(returnSame());
    }});

    myTask.checkForUpdates();
    m.assertIsSatisfied();
  }

  private PackageCheckRequest createRequest() {
    return new PackageCheckRequestFactory(new PackageCheckerSettingsImpl()).createRequest(mode1, new SourcePackageReference(null, "id", null));
  }

  private final CheckRequestMode mode1 = new CheckRequestMode() {  };

  private CustomAction executeRunnable() {
    return new CustomAction("execute") {
      public Object invoke(Invocation invocation) throws Throwable {
        ((Runnable)invocation.getParameter(0)).run();
        return null;
      }
    };
  }

  private CustomAction returnSame() {
    return new CustomAction("return same") {
      public Object invoke(Invocation invocation) throws Throwable {
        return invocation.getParameter(0);
      }
    };
  }

  private PackageCheckEntry createEntry() {
    return new PackageCheckEntry(createRequest(), myTime, new PackageCheckerSettingsImpl());
  }


}
