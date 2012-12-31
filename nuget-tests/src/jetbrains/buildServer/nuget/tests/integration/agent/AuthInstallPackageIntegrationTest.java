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
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.nuget.tests.integration.http.HttpAuthServer;
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
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
  private String myDownloadUrl;
  private String myUser;
  private String myPassword;
  private AtomicBoolean myIsAuthorized;
  private List<String> myAuthSource;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m.checking(new Expectations() {{
      allowing(myLogger).activityStarted(with(equal("authenticate")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("authenticate")), with(any(String.class)));
    }});


    myUser = "u-" + StringUtil.generateUniqueHash();
    myPassword = "p-" + StringUtil.generateUniqueHash();
    myIsAuthorized = new AtomicBoolean(false);

    myHttp = new HttpAuthServer() {
      @Override
      protected Response getAuthorizedResponse(String request) throws IOException {
        final String path = getRequestPath(request);
        if (path == null) return createStreamResponse(STATUS_LINE_404, Collections.<String>emptyList(), "Not found".getBytes("utf-8"));
        System.out.println("NuGet request path: " + path);

        final Collection<String> xml = Arrays.asList("DataServiceVersion: 1.0;", "Content-Type: application/xml;charset=utf-8");
        final Collection<String> atom = Arrays.asList("DataServiceVersion: 2.0;", "Content-Type: application/atom+xml;charset=utf-8");

        if (path.endsWith("$metadata")) {
          return createStringResponse(STATUS_LINE_200, xml, loadMockODataFiles("feed/mock/feed.metadata.xml"));
        }

        if (path.contains("nuget/Packages()") && path.contains("?$filter=")) {
          return createStringResponse(STATUS_LINE_200, atom, loadMockODataFiles("feed/mock/feed.package.xml"));
        }

        if (path.contains("nuget/Packages")) {
          return createStringResponse(STATUS_LINE_200, atom, loadMockODataFiles("feed/mock/feed.packages.xml"));
        }

        if (path.contains("nuget")) {
          return createStringResponse(STATUS_LINE_200, xml, loadMockODataFiles("feed/mock/feed.root.xml"));
        }

        if (path.contains("FineCollection.1.0.189.152.nupkg")) {
          return getFileResponse(Paths.getTestDataPath("feed/mock/FineCollection.1.0.189.152.nupkg"), Arrays.asList("Content-Type: application/zip"));
        }

        return createStringResponse(STATUS_LINE_404, Collections.<String>emptyList(), "Not found");
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
    mySourceUrl = "http://localhost:" + myHttp.getPort() + "/nuget/";
    myDownloadUrl = "http://localhost:" + myHttp.getPort() + "/download/";
    myAuthSource = Arrays.asList(mySourceUrl);
  }

  private String loadMockODataFiles(@NotNull String name) throws IOException {
    String source = loadFileUTF8(name);
    source = source.replace("http://buildserver.labs.intellij.net/httpAuth/app/nuget/v1/FeedService.svc/", mySourceUrl);
    source = source.replace("http://buildserver.labs.intellij.net/httpAuth/repository/download/", myDownloadUrl);
    source = source.replaceAll("xml:base=\".*\"", "xml:base=\"" + mySourceUrl + "\"");
    return source;
  }

  @NotNull
  private String loadFileUTF8(@NotNull String name) throws IOException {
    File file = Paths.getTestDataPath(name);
    return loadFileUTF8(file);
  }

  private String loadFileUTF8(@NotNull File file) throws IOException {
    final InputStream is = new BufferedInputStream(new FileInputStream(file));
    try {
      final Reader rdr = new InputStreamReader(is, "utf-8");
      StringBuilder sb = new StringBuilder();
      int ch;
      while ((ch = rdr.read()) >= 0) {
        sb.append((char) ch);
      }
      return sb.toString();
    } finally {
      FileUtil.close(is);
    }
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    myHttp.stop();
  }

  @Test(dataProvider = NUGET_VERSIONS_20p)
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

  @Test(dataProvider = NUGET_VERSIONS_20p)
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
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);
    fetchPackages(new File(myRoot, "sln1-lib.sln"), myAuthSource, false, true, false, nuget, null, null);
    Assert.assertTrue(myIsAuthorized.get(), "NuGet must authorize");
  }


  @Test(dataProvider = NUGET_VERSIONS_20p)
  public void test_auth_update(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    m.checking(new Expectations() {{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdate).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdate).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(myUpdate).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdate).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_SLN));
    }});

    fetchPackages(new File(myRoot, "sln1-lib.sln"), myAuthSource, false, true, true, nuget, null);

    Assert.assertTrue(myIsAuthorized.get(), "NuGet must authorize");
  }

}
