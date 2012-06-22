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

package jetbrains.buildServer.nuget.tests.integration.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.tests.agent.PackageSourceImpl;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.nuget.tests.integration.http.HttpAuthServer;
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 16:47
 */
public class AuthInstallPackageIntegrationTest extends InstallPackageIntegrationTestCase {
  private HttpAuthServer myHttp;
  private String mySourceUrl;
  private String myUser;
  private String myPassword;
  private AtomicBoolean myIsAuthorized;
  private List<PackageSource> myAuthSource;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUser = "u-" + StringUtil.generateUniqueHash();
    myPassword = "p-" + StringUtil.generateUniqueHash();
    myIsAuthorized = new AtomicBoolean(false);


    myHttp = new HttpAuthServer() {
      @Override
      protected Response getAuthorizedResponse(String request) throws IOException {
        return createStreamResponse(STATUS_LINE_404, Collections.<String>emptyList(), "Not found".getBytes("utf-8"));
      }

      @Override
      protected boolean authorizeUser(@NotNull String loginPassword) {
        if ((myUser + ":" + myPassword).equals(loginPassword)) {
          myIsAuthorized.set(true);
          return true;
        }
        return false;
      }
    };

    myHttp.start();
    mySourceUrl = "http://localhost:" + myHttp.getPort() + "/nuget";
    myAuthSource = Arrays.<PackageSource>asList(new PackageSourceImpl(mySourceUrl, myUser, myPassword));
  }


  @Test(dataProvider = NUGET_VERSIONS)
  public void test_auth_install(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), myAuthSource, false, false, nuget, null);

    Assert.assertTrue(myIsAuthorized.get(), "NuGet must authorize");
  }


}
