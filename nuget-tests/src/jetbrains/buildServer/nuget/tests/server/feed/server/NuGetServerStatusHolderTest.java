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

package jetbrains.buildServer.nuget.tests.server.feed.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerStatus;
import jetbrains.buildServer.nuget.server.feed.server.process.NuGetServerStatusHolderImpl;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.11.11 17:44
 */
public class NuGetServerStatusHolderTest extends BaseTestCase {
  private Mockery m;
  private NuGetServerRunnerSettings mySettings;
  private NuGetServerStatusHolderImpl myHolder;

  private boolean myIsEnabled;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    m = new Mockery();
    myIsEnabled = false;
    mySettings = m.mock(NuGetServerRunnerSettings.class);
    m.checking(new Expectations(){{
      allowing(mySettings).isNuGetFeedEnabled(); will(new CustomAction("return myIsEnabled") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myIsEnabled;
        }
      });
      allowing(mySettings).getLogFilePath(); will(returnValue(createTempFile("aaa")));
    }});
    myHolder = new NuGetServerStatusHolderImpl(mySettings);
  }

  @Test
  public void test_remember_failed_ping_status_on_stop() {
    myHolder.startingServer();
    myHolder.pingFailed();
    myHolder.stoppingServer();

    final NuGetServerStatus status = myHolder.getStatus();
    Assert.assertFalse(status.isRunning());
    Assert.assertFalse(status.getServerAccessible());
  }

  @Test
  public void test_remember_succeeded_ping_status_on_stop() {
    myHolder.startingServer();
    myHolder.pingSucceeded();
    myHolder.stoppingServer();

    final NuGetServerStatus status = myHolder.getStatus();
    Assert.assertFalse(status.isRunning());
    Assert.assertTrue(status.getServerAccessible());
  }

  @Test
  public void test_remember_failed_ping_status_on_restart() {
    myHolder.startingServer();
    myHolder.pingFailed();
    myHolder.stoppingServer();
    myHolder.startingServer();

    final NuGetServerStatus status = myHolder.getStatus();
    Assert.assertTrue(status.isRunning());
    Assert.assertFalse(status.getServerAccessible());
  }
  @Test
  public void test_remember_succeeded_ping_status_on_restart() {
    myHolder.startingServer();
    myHolder.pingSucceeded();
    myHolder.stoppingServer();
    myHolder.startingServer();

    final NuGetServerStatus status = myHolder.getStatus();
    Assert.assertTrue(status.isRunning());
    Assert.assertTrue(status.getServerAccessible());
  }

  @Test
  public void test_default_disabled() {
    final NuGetServerStatus status = myHolder.getStatus();

    Assert.assertFalse(status.isRunning());
    Assert.assertFalse(status.isScheduledToStart());
    Assert.assertNull(status.getServerAccessible());
  }

  @Test
  public void test_default_enabled() {
    myIsEnabled = true;
    final NuGetServerStatus status = myHolder.getStatus();

    Assert.assertFalse(status.isRunning());
    Assert.assertTrue(status.isScheduledToStart());
    Assert.assertNull(status.getServerAccessible());
  }

}
