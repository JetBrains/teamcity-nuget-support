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

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.trigger.impl.*;
import jetbrains.buildServer.util.TimeService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myChecker1 = m.mock(PackageChecker.class, "checker1");
    myChecker2 = m.mock(PackageChecker.class, "checker2");
    myService = m.mock(ScheduledExecutorService.class);
    myQueue = m.mock(PackageCheckQueue.class);
    myTime = m.mock(TimeService.class);

    myTask = new PackageChangesCheckerThreadTask(myQueue, myService, Arrays.asList(myChecker1, myChecker2));

    m.checking(new Expectations(){{
      allowing(myTime).now(); will(returnValue(1L));
    }});
  }

  @Test
  public void test_schedule_empty() {
    m.checking(new Expectations(){{
      allowing(myQueue).getItemsToCheckNow(); will(returnValue(Collections.emptyList()));
      oneOf(myQueue).cleaupObsolete();
      oneOf(myQueue).getSleepTime(); will(returnValue(1234L));

      oneOf(myService).schedule(with(any(Runnable.class)), with(equal(1234L)), with(equal(TimeUnit.MILLISECONDS)));
    }});

    myTask.checkForUpdates();
  }

  @Test
  public void test_schecule_execution() {
    final PackageCheckEntry req = createEntry();

    m.checking(new Expectations(){{
      oneOf(myQueue).getItemsToCheckNow(); will(returnValue(Arrays.asList(req)));
      oneOf(myQueue).getItemsToCheckNow(); will(returnValue(Collections.emptyList()));
      oneOf(myQueue).cleaupObsolete();

      oneOf(myChecker1).accept(req.getRequest()); will(returnValue(true));
      oneOf(myChecker1).update(with(equal(myService)), with(equal(Arrays.<CheckablePackage>asList(req))));

      oneOf(myQueue).getSleepTime(); will(returnValue(1234L));
      oneOf(myService).schedule(with(any(Runnable.class)), with(any(long.class)), with(any(TimeUnit.class)));
    }});

    myTask.checkForUpdates();
  }

  private PackageCheckEntry createEntry() {
    return new PackageCheckEntry(createRequest(), myTime, new PackageCheckerSettingsImpl());
  }

  @Test
  public void test_schecule_execution2() {
    final PackageCheckEntry req = createEntry();

    m.checking(new Expectations(){{
      oneOf(myQueue).getItemsToCheckNow(); will(returnValue(Arrays.asList(req)));
      oneOf(myQueue).getItemsToCheckNow(); will(returnValue(Collections.emptyList()));
      oneOf(myQueue).cleaupObsolete();

      oneOf(myChecker1).accept(req.getRequest()); will(returnValue(false));
      oneOf(myChecker2).accept(req.getRequest()); will(returnValue(true));
      oneOf(myChecker2).update(with(equal(myService)), with(equal(Arrays.<CheckablePackage>asList(req))));

      oneOf(myQueue).getSleepTime(); will(returnValue(1234L));
      oneOf(myService).schedule(with(any(Runnable.class)), with(any(long.class)), with(any(TimeUnit.class)));
    }});

    myTask.checkForUpdates();
  }

  private PackageCheckRequest createRequest() {
    return new PackageCheckRequestFactory(new PackageCheckerSettingsImpl()).createRequest(mode1, null, "id", null);
  }

  private final CheckRequestMode mode1 = new CheckRequestMode() {  };
}
