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

package jetbrains.buildServer.nuget.tests.integration.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.nuget.tests.integration.MockNuGetAuthHTTP;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
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
    myAuthSource = Collections.singletonList(myHttp.getSourceUrl());
    addGlobalSource(myHttp.getSourceUrl(), myHttp.getUsername(), myHttp.getPassword());
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    myHttp.stop();
    super.tearDown();
  }

  @Test(dataProvider = NUGET_VERSIONS_20p)
  public void test_auth_install(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01-mockFeed.zip"), "", myRoot);
    FileUtil.delete(new File(myRoot,"packages"));
    fetchPackages(nuget);
    Assert.assertTrue(myHttp.getIsAuthorized().get(), "NuGet must authorize");
  }

  @Test(dataProvider = NUGET_VERSIONS_28p)
  public void test_auth_restore(@NotNull final NuGet nuget) throws RunBuildException {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;
    ArchiveUtil.unpackZip(getTestDataPath("test-01-mockFeed.zip"), "", myRoot);
    FileUtil.delete(new File(myRoot,"packages"));

    fetchPackages(nuget);
    Assert.assertTrue(myHttp.getIsAuthorized().get(), "NuGet must authorize");
  }

  @Test(dataProvider = NUGET_VERSIONS_20p)
  public void test_auth_update(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01-mockFeed.zip"), "", myRoot);
    FileUtil.delete(new File(myRoot,"packages"));

    m.checking(new Expectations() {{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdateParameters).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdateParameters).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(myUpdateParameters).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdateParameters).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_SLN));
    }});

    fetchPackages(nuget);

    Assert.assertTrue(myHttp.getIsAuthorized().get(), "NuGet must authorize");
  }

  private void fetchPackages(@NotNull NuGet nuget) throws RunBuildException {
    fetchPackages(new File(myRoot, "sln1-lib.sln"), myAuthSource, false, true, false, nuget, null, null);
  }
}
