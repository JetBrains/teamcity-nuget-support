/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.FetchException;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolsPolicy;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.AvailableToolsState;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.AvailableToolsStateImpl;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.util.TimeService;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 29.08.11 18:55
 */
public class AvailableToolStateTest extends BaseTestCase {
  private Mockery m;
  private FeedClient myClient;
  private AvailableToolsState myState;
  private NuGetFeedReader myReader;
  private TimeService myTime;



  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myClient = m.mock(FeedClient.class);
    myReader = m.mock(NuGetFeedReader.class);
    myTime = m.mock(TimeService.class);

    myState = new AvailableToolsStateImpl(myClient, myReader, myTime);

    m.checking(new Expectations(){{
      allowing(myClient).withCredentials(null); will(returnValue(myClient));
    }});
  }

  @Test
  public void test_should_try_both_feeds_on_error() throws IOException {

    m.checking(new Expectations(){{
      oneOf(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v1/FeedService.svc", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      oneOf(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      allowing(myTime).now(); will(returnValue(1000234L));
    }});

    try {
      myState.getAvailable(ToolsPolicy.FetchNew);
      Assert.fail();
    } catch (FetchException e) {
    }

    m.assertIsSatisfied();
  }

  @Test
  public void test_should_work_on_one_feed_error_1() throws IOException, FetchException {

    m.checking(new Expectations(){{
      allowing(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      oneOf(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v1/FeedService.svc", "NuGet.CommandLine"); will(returnValue(Collections.emptyList()));
      allowing(myTime).now(); will(returnValue(1000234L));
    }});

    myState.getAvailable(ToolsPolicy.FetchNew);
    m.assertIsSatisfied();
  }

  @Test
  public void test_should_work_on_one_feed_error_2() throws IOException, FetchException {

    m.checking(new Expectations(){{
      oneOf(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(returnValue(Collections.emptyList()));
      allowing(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v1/FeedService.svc", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      allowing(myTime).now(); will(returnValue(1000234L));
    }});

    myState.getAvailable(ToolsPolicy.FetchNew);
    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-30395")
  public void test_should_return_newer_first() throws IOException, FetchException {

    m.checking(new Expectations(){{
      oneOf(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(returnValue(Arrays.asList(commandLine("2.0.1"), commandLine("2.7.0"), commandLine("1.4.2"))));
      allowing(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v1/FeedService.svc", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      allowing(myTime).now(); will(returnValue(1000234L));
    }});

    Iterator<? extends NuGetTool> tools = myState.getAvailable(ToolsPolicy.FetchNew).iterator();
    Assert.assertEquals(tools.next().getVersion(), "2.7.0");
    Assert.assertEquals(tools.next().getVersion(), "2.0.1");
    Assert.assertEquals(tools.next().getVersion(), "1.4.2");
    Assert.assertFalse(tools.hasNext());

    m.assertIsSatisfied();
  }

  @NotNull
  private FeedPackage commandLine(@NotNull String version) {
    return new FeedPackage("atomId", new PackageInfo("pkd", version), false, "", "download-url");
  }
}
