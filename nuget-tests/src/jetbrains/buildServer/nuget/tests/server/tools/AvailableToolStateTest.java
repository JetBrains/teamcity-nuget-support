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

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.FetchException;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolsPolicy;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.AvailableToolsState;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.AvailableToolsStateImpl;
import jetbrains.buildServer.util.TimeService;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 29.08.11 18:55
 */
public class AvailableToolStateTest extends BaseTestCase {
  private Mockery m;
  private AvailableToolsState myState;
  private NuGetFeedReader myReader;
  private TimeService myTime;



  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myReader = m.mock(NuGetFeedReader.class);
    myTime = m.mock(TimeService.class);

    myState = new AvailableToolsStateImpl(myReader, myTime);
  }

  @Test
  public void test_should_try_both_feeds_on_error() throws IOException {

    m.checking(new Expectations(){{
      oneOf(myReader).queryPackageVersions("http://packages.nuget.org/api/v1/FeedService.svc", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      oneOf(myReader).queryPackageVersions("http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      oneOf(myReader).queryPackageVersions("https://go.microsoft.com/fwlink/?LinkID=206669", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      oneOf(myReader).queryPackageVersions("https://go.microsoft.com/fwlink/?LinkID=230477", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
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
      allowing(myReader).queryPackageVersions("http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      oneOf(myReader).queryPackageVersions("https://go.microsoft.com/fwlink/?LinkID=230477", "NuGet.CommandLine"); will(returnValue(Collections.emptyList()));
      allowing(myTime).now(); will(returnValue(1000234L));
    }});

    myState.getAvailable(ToolsPolicy.FetchNew);
    m.assertIsSatisfied();
  }

  @Test
  public void test_should_work_on_one_feed_error_2() throws IOException, FetchException {

    m.checking(new Expectations(){{
      oneOf(myReader).queryPackageVersions("http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(returnValue(Collections.emptyList()));
      allowing(myReader).queryPackageVersions("https://go.microsoft.com/fwlink/?LinkID=230477", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      allowing(myTime).now(); will(returnValue(1000234L));
    }});

    myState.getAvailable(ToolsPolicy.FetchNew);
    m.assertIsSatisfied();
  }
}
