/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.nuget.tests.integration.MockNuGetAuthHTTP;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.util.ArchiveUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 16:47
 */
public class AuthInstallPackageIntegrationTest extends InstallPackageIntegrationTestCase {
  private MockNuGetAuthHTTP myHttp;
  private List<String> myAuthSource;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myHttp = new MockNuGetAuthHTTP();
    myHttp.start();
    myAuthSource = Arrays.asList(myHttp.getSourceUrl());
    addGlobalSource(myHttp.getSourceUrl(), myHttp.getUsername(), myHttp.getPassword());
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    myHttp.stop();
  }

  @Test(dataProvider = NUGET_VERSIONS_20p, enabled = false)
  public void test_bare_commands_list(@NotNull final NuGet nuget) throws RunBuildException, IOException {
    m.checking(new Expectations() {{
      allowing(myNuGet).getNuGetExeFile();
      will(returnValue(nuget.getPath()));
    }});

    File wd = createTempDir(); //myWorkdirCalculator.getNuGetWorkDir(myContext, myWorkDir);
    //BuildProcess auth = myActionFactory.createAuthenticateFeeds(myContext, myAuthSource, myNuGet);
    BuildProcess list = myExecutor.executeCommandLine(myContext, nuget.getPath().getPath(), Arrays.asList("list", "-AllVersions"), wd, Collections.<String, String>emptyMap());

    //assertRunSuccessfully(auth, BuildFinishedStatus.FINISHED_SUCCESS);
    //File config = new File(wd, "NuGet.config");
    //Assert.assertTrue(config.isFile());
    //System.out.println("NuGet.Config: " + loadFileUTF8(config));

    assertRunSuccessfully(list, BuildFinishedStatus.FINISHED_SUCCESS);
    Assert.assertTrue(getCommandsOutput().contains("FineCollection 1.0.189"));
    Assert.assertTrue(getCommandsOutput().contains("TestUtils 1.0.189"));
  }

  @Test(dataProvider = NUGET_VERSIONS_20p, enabled = false)
  public void test_bare_commands_install(@NotNull final NuGet nuget) throws RunBuildException, IOException {
    m.checking(new Expectations() {{
      allowing(myNuGet).getNuGetExeFile();
      will(returnValue(nuget.getPath()));
    }});

    File wd = createTempDir(); //myWorkdirCalculator.getNuGetWorkDir(myContext, myWorkDir);
    //    BuildProcess auth = myActionFactory.createAuthenticateFeeds(myContext, myAuthSource, myNuGet);
    BuildProcess list = myExecutor.executeCommandLine(myContext, nuget.getPath().getPath(), Arrays.asList("install", "FineCollection"), wd, Collections.<String, String>emptyMap());

    //    assertRunSuccessfully(auth, BuildFinishedStatus.FINISHED_SUCCESS);
    //    File config = new File(wd, "NuGet.config");
    //    Assert.assertTrue(config.isFile());
    //    System.out.println("NuGet.Config: " + loadFileUTF8(config));

    assertRunSuccessfully(list, BuildFinishedStatus.FINISHED_SUCCESS);
    Assert.assertTrue(getCommandsOutput().contains("FineCollection 1.0.189"));
    Assert.assertFalse(getCommandsOutput().contains("TestUtils 1.0.189"));
  }

  @Test(dataProvider = NUGET_VERSIONS_20p)
  public void test_auth_install(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01-mockFeed.zip"), "", myRoot);
    fetchPackages(new File(myRoot, "sln1-lib.sln"), myAuthSource, false, true, false, nuget, null, null);
    Assert.assertTrue(myHttp.getIsAuthorized().get(), "NuGet must authorize");
  }

  @Test(dataProvider = NUGET_VERSIONS_27p)
  public void test_auth_restore(@NotNull final NuGet nuget) throws RunBuildException {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;
    ArchiveUtil.unpackZip(getTestDataPath("test-01-mockFeed.zip"), "", myRoot);
    fetchPackages(new File(myRoot, "sln1-lib.sln"), myAuthSource, false, true, false, nuget, null, null);
    Assert.assertTrue(myHttp.getIsAuthorized().get(), "NuGet must authorize");
  }

  @Test(dataProvider = NUGET_VERSIONS_20p)
  public void test_auth_update(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01-mockFeed.zip"), "", myRoot);

    m.checking(new Expectations() {{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdate).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdate).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(myUpdate).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdate).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_SLN));
    }});

    fetchPackages(new File(myRoot, "sln1-lib.sln"), myAuthSource, false, true, false, nuget, null, null);

    Assert.assertTrue(myHttp.getIsAuthorized().get(), "NuGet must authorize");
  }

}
