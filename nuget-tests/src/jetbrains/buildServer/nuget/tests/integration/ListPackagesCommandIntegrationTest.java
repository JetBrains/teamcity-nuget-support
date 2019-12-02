/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.TempFolderProvider;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedCredentials;
import jetbrains.buildServer.nuget.server.exec.*;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackagesCommandImpl;
import jetbrains.buildServer.nuget.server.exec.impl.NuGetExecutorImpl;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 21:24
 */
public class ListPackagesCommandIntegrationTest extends IntegrationTestBase {
  private ListPackagesCommand myCommand;

  @BeforeMethod
  @Override
  public void setUp() throws Exception {
    super.setUp();

    myCommand = createMockCommand(myNuGetTeamCityProvider, createTempDir());
    enableExecution("./../nuget-extensions/bin/JetBrains.TeamCity.NuGetRunner.exe");
  }

  @NotNull
  public static ListPackagesCommand createMockCommand(@NotNull final NuGetTeamCityProvider provider, @NotNull final File tempDir) {
    Mockery m = new Mockery();
    final SystemInfo info = m.mock(SystemInfo.class);
    final TempFolderProvider temp = m.mock(TempFolderProvider.class);

    m.checking(new Expectations(){{
      allowing(info).canStartNuGetProcesses(); will(returnValue(true));
      allowing(temp).getTempDirectory(); will(returnValue(tempDir));
    }});

    Logger.getLogger(NuGetExecutorImpl.class.getName()).setLevel(Level.DEBUG);
    return new ListPackagesCommandImpl(new NuGetExecutorImpl(provider, info, temp), temp);
  }

  @AfterMethod
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_batch_reportNUnitAndYouTrackSharp_from_default_feed(@NotNull final NuGet nuget) throws Throwable {
    MockNuGetHTTP.executeTest(new MockNuGetHTTP.Action() {
      public void runTest(@NotNull MockNuGetHTTP http) throws Throwable {
        final SourcePackageReference nunit_all = new SourcePackageReference(http.getSourceUrl(), "NUnit", null);
        final SourcePackageReference nunit_filter = new SourcePackageReference(http.getSourceUrl(), "NUnit", "(1.1.1.1, 2.5.9.1)");
        final SourcePackageReference youTrackSharp = new SourcePackageReference(http.getSourceUrl(), "YouTrackSharp", null);
        final List<SourcePackageReference> packages = Arrays.asList(
          nunit_all,
          nunit_filter,
          youTrackSharp
        );

        Map<SourcePackageReference, ListPackagesResult> checkForUpdatesResults = myCommand.checkForChanges(nuget.getPath(), packages);

        assertEquals(3, checkForUpdatesResults.size());
        System.out.println("m = " + checkForUpdatesResults);

        for (ListPackagesResult infos : checkForUpdatesResults.values()) {
          assertNull(infos.getErrorMessage());
          assertNotEmpty(infos.getCollectedInfos());
        }

        final Collection<SourcePackageInfo> nAll = checkForUpdatesResults.get(nunit_all).getCollectedInfos();
        final Collection<SourcePackageInfo> nFilter = checkForUpdatesResults.get(nunit_filter).getCollectedInfos();
        final Collection<SourcePackageInfo> nYouTrack = checkForUpdatesResults.get(youTrackSharp).getCollectedInfos();

        assertEquals(new ArrayList<SourcePackageInfo>(nAll).toString(), 1, nAll.size());
        assertEquals(new ArrayList<SourcePackageInfo>(nYouTrack).toString(), 1, nYouTrack.size());
        assertNotEmpty(nFilter);
      }
    });
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_batch_reportNUnitAndYouTrackSharp_from_default_feed_x2(@NotNull final NuGet nuget) throws Throwable {
    doTriggerTest(nuget, "NUnit", "YouTrackSharp");
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_batch_reportNUnitAndYouTrackSharp_from_default_feed_x3(@NotNull final NuGet nuget) throws Throwable {
    doTriggerTest(nuget, "NUnit", "YouTrackSharp", "EASYHTTP");
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_batch_reportNUnitAndYouTrackSharp_from_default_feed_x4(@NotNull final NuGet nuget) throws Throwable {
    doTriggerTest(nuget, "NUnit", "EasyHttp", "Elmah", "jquery");
  }

  @Test(dataProvider = NUGET_VERSIONS_20p)
  public void test_auth_supported(@NotNull final NuGet nuget) throws Throwable {
    MockNuGetAuthHTTP.executeTest(new MockNuGetAuthHTTP.Action() {
      public void runTest(@NotNull MockNuGetAuthHTTP http) throws Throwable {

        final List<SourcePackageReference> allRefs = Arrays.asList(
                new SourcePackageReference(http.getSourceUrl(), http.getCredentials(), http.getPackageId(), null, false)
        );

        assertPackages(allRefs, myCommand.checkForChanges(nuget.getPath(), allRefs));
      }
    });
  }

  @Test(dataProvider = NUGET_VERSIONS_20p)
  public void test_auth_supported_wrong_credentials(@NotNull final NuGet nuget) throws Throwable {
    MockNuGetAuthHTTP.executeTest(new MockNuGetAuthHTTP.Action() {
      public void runTest(@NotNull MockNuGetAuthHTTP http) throws Throwable {

        final List<SourcePackageReference> allRefs = Arrays.asList(
                new SourcePackageReference(http.getSourceUrl(), new NuGetFeedCredentials("aaa", "bbb"), http.getPackageId(), null, false)
        );

        Map<SourcePackageReference, ListPackagesResult> result = myCommand.checkForChanges(nuget.getPath(), allRefs);
        Assert.assertEquals(result.size(), 1);
        ListPackagesResult res = result.values().iterator().next();
        Assert.assertTrue(res.getCollectedInfos().isEmpty());
        String msg = res.getErrorMessage();
        Assert.assertNotNull(msg);
        Assert.assertTrue(msg.toLowerCase().contains("401") || msg.toLowerCase().contains("Cannot prompt for input".toLowerCase()));
        Assert.assertEquals(result.keySet().iterator().next(), allRefs.get(0));
      }
    });
  }

  protected void doTriggerTest(@NotNull final NuGet nuget, @NotNull String... packageNames) throws Throwable {
    doTriggerTest(myCommand, nuget, packageNames);
  }

  public static void doTriggerTest(@NotNull ListPackagesCommand myCommand,
                               @NotNull final NuGet nuget,
                               @NotNull String feed,
                               @NotNull String... packageNames) throws NuGetExecutionException {

    final List<SourcePackageReference> allRefs = new ArrayList<SourcePackageReference>();
    for (String s : packageNames) {
      allRefs.add(new SourcePackageReference(feed, s, null));
    }

    assertPackages(allRefs, myCommand.checkForChanges(nuget.getPath(), allRefs));
  }

  public static void doTriggerTest(@NotNull ListPackagesCommand myCommand,
                               @NotNull final NuGet nuget,
                               @NotNull String... packageNames) throws Throwable {

    MockNuGetHTTP.executeTest(new MockNuGetHTTP.Action() {
      public void runTest(@NotNull MockNuGetHTTP http) throws Throwable {
        final List<SourcePackageReference> allRefs = new ArrayList<SourcePackageReference>();

        for (String s : packageNames) {
          allRefs.add(new SourcePackageReference(http.getSourceUrl(), s, null));
        }
        assertPackages(allRefs, myCommand.checkForChanges(nuget.getPath(), allRefs));
      }
    });
  }

  private static void assertPackages(@NotNull List<SourcePackageReference> allRefs,
                                     @NotNull Map<SourcePackageReference, ListPackagesResult> m1) {
    System.out.println("result: " + m1);
    for (SourcePackageReference allRef : allRefs) {
      Collection<SourcePackageInfo> i = m1.get(allRef).getCollectedInfos();
      Assert.assertNotNull(i);
      Assert.assertTrue(i.size() == 1, "should return version for " + allRef);
    }
    Assert.assertEquals(m1.size(), allRefs.size());

    for (ListPackagesResult infos : m1.values()) {
      Assert.assertTrue(infos.getCollectedInfos().size() == 1);
    }
  }
}
