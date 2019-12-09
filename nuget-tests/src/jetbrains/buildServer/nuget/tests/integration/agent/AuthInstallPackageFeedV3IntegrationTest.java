/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.TestNGUtil;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.tests.integration.MockNuGetAuthHTTP;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
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
public class AuthInstallPackageFeedV3IntegrationTest extends InstallPackageIntegrationTestCase {
  private MockNuGetAuthHTTP myHttp;
  private List<String> myAuthSource;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myHttp = new MockNuGetAuthHTTP(NuGetAPIVersion.V3);
    myHttp.start();
    String feedUrl = StringUtil.trimEnd(myHttp.getSourceUrl(), "/") + "/index.json";
    myAuthSource = Collections.singletonList(feedUrl);
    addGlobalSource(feedUrl, myHttp.getUsername(), myHttp.getPassword());
    myHttp
      .withPackage("FineCollection", "2.2.1", "feed/mock/feed.finecollection.2.2.1.package.xml", "feed/mock/feed.finecollection.2.2.1.nupkg", true);
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    myHttp.stop();
    super.tearDown();
  }

  @Test(dataProvider = NUGET_VERSIONS_48p)
  public void test_auth_restore(@NotNull final NuGet nuget) throws RunBuildException {
    if (!SystemInfo.isWindows && nuget == NuGet.NuGet_4_8) {
      TestNGUtil.skip("Credentials plugin in NuGet 4.8 does not work on Mono");
    }

    myInstallMode = PackagesInstallMode.VIA_RESTORE;
    ArchiveUtil.unpackZip(getTestDataPath("test-01-mockFeed.zip"), "", myRoot);
    fetchPackages(nuget);
    Assert.assertTrue(myHttp.getIsAuthorized(), "NuGet must authorize");

    Assert.assertTrue(getCommandsOutput().contains("Added package 'FineCollection.2.2.1'"));
  }

  private void fetchPackages(@NotNull NuGet nuget) throws RunBuildException {
    fetchPackages(new File(myRoot, "sln1-lib.sln"), myAuthSource, false, true, false, nuget, null, null);
  }
}
