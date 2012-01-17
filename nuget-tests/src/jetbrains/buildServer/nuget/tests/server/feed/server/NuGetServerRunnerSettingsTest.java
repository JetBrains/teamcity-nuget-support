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
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerDotNetSettingsEx;
import jetbrains.buildServer.nuget.server.feed.server.dotNetFeed.MetadataControllersPaths;
import jetbrains.buildServer.nuget.server.feed.server.impl.NuGetServerRunnerSettingsImpl;
import jetbrains.buildServer.nuget.server.settings.impl.NuGetSettingsManagerImpl;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 10.11.11 10:12
 */
public class NuGetServerRunnerSettingsTest extends BaseTestCase {
  private Mockery m;
  private RootUrlHolder myRoot;
  private MetadataControllersPaths myPaths;
  private ServerPaths myServerPaths;
  private SystemInfo mySystemInfo;
  private NuGetServerDotNetSettingsEx mySettings;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();
    myRoot = m.mock(RootUrlHolder.class);
    myPaths = m.mock(MetadataControllersPaths.class);
    myServerPaths = new ServerPaths(createTempDir().getPath());
    mySystemInfo = m.mock(SystemInfo.class);

    mySettings = new NuGetServerRunnerSettingsImpl(
            myRoot,
            myPaths,
            myServerPaths,
            new NuGetSettingsManagerImpl(),
            mySystemInfo
    );
  }


  @Test
  public void test_feed_disabled_by_default() {
    m.checking(new Expectations(){{
      allowing(mySystemInfo).canStartNuGetProcesses(); will(returnValue(true));
    }});

    Assert.assertFalse(mySettings.isNuGetFeedEnabled());
  }

  @Test
  public void test_feed_enable() {
    m.checking(new Expectations(){{
      allowing(mySystemInfo).canStartNuGetProcesses(); will(returnValue(true));
    }});

    mySettings.setNuGetFeedEnabled(true);
    Assert.assertTrue(mySettings.isNuGetFeedEnabled());
  }

  @Test
  public void test_feed_enable_linux() {
    m.checking(new Expectations(){{
      allowing(mySystemInfo).canStartNuGetProcesses(); will(returnValue(false));
    }});

    mySettings.setNuGetFeedEnabled(true);
    Assert.assertFalse(mySettings.isNuGetFeedEnabled());
  }

  @Test
  public void test_use_default_server_url() {
    m.checking(new Expectations(){{
      allowing(myRoot).getRootUrl(); will(returnValue("http://jonnyzzz.name/teamcity"));
      allowing(myPaths).getBasePath(); will(returnValue("nuget5"));
    }});

    Assert.assertEquals(mySettings.getTeamCityBackBaseUrl(), "http://jonnyzzz.name/teamcity/nuget5");
    Assert.assertNull(mySettings.getCustomTeamCityBaseUrl());
  }

  @Test
  public void test_use_custom_server_url() {
    m.checking(new Expectations(){{
      allowing(myPaths).getBasePath(); will(returnValue("nuget5"));
      oneOf(myRoot).getRootUrl(); will(returnValue("aaa"));
    }});

    mySettings.setTeamCityBaseUrl("http://jonnyzzz.com/n");
    Assert.assertEquals(mySettings.getTeamCityBackBaseUrl(), "http://jonnyzzz.com/n/nuget5");
    Assert.assertEquals(mySettings.getCustomTeamCityBaseUrl(), "http://jonnyzzz.com/n");
  }

  @Test
  public void test_use_custom_server_url3() {
    m.checking(new Expectations(){{
      allowing(myPaths).getBasePath(); will(returnValue("nuget5"));
      oneOf(myRoot).getRootUrl(); will(returnValue("aaa"));
    }});

    mySettings.setTeamCityBaseUrl("aaa");
    Assert.assertEquals(mySettings.getCustomTeamCityBaseUrl(), null);
  }

  @Test
  public void test_use_custom_server_url2() {
    m.checking(new Expectations(){{
      allowing(myPaths).getBasePath(); will(returnValue("/nuget5"));
      allowing(myRoot).getRootUrl(); will(returnValue("ooo"));
    }});

    mySettings.setTeamCityBaseUrl("http://jonnyzzz.com/n/");
    Assert.assertEquals(mySettings.getTeamCityBackBaseUrl(), "http://jonnyzzz.com/n/nuget5");
    Assert.assertEquals(mySettings.getCustomTeamCityBaseUrl(), "http://jonnyzzz.com/n/");
  }

  @Test
  public void test_reset_custom_server_url() {
    test_use_custom_server_url();
    m.checking(new Expectations(){{
      allowing(myRoot).getRootUrl(); will(returnValue("http://jonnyzzz.name/teamcity"));
    }});

    mySettings.setDefaultTeamCityBaseUrl();
    Assert.assertEquals(mySettings.getTeamCityBackBaseUrl(), "http://jonnyzzz.name/teamcity/nuget5");
    Assert.assertNull(mySettings.getCustomTeamCityBaseUrl());
  }
}
