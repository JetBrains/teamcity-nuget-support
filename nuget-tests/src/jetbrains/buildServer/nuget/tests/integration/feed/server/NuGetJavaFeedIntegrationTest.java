/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.HttpServer;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.tests.integration.ListPackagesCommandIntegrationTest;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.01.12 0:40
 */
public class NuGetJavaFeedIntegrationTest extends NuGetJavaFeedIntegrationTestBase {
  private final String packageId_1 = "CommonServiceLocator";
  private final String packageId_2 = "NuGet.Core";
  private URI myServerUrl;
  private HttpServer myServer;

  @Override
  protected String getNuGetServerUrl() {
    return myServerUrl.toString() + NuGetJavaFeedIntegrationTestBase.SERVLET_PATH.substring(1);
  }

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    addPackage(Paths.getTestDataPath("/packages/" + packageId_1 + ".1.0.nupkg"), true);
    addPackage(Paths.getTestDataPath("/packages/" + packageId_2 + ".1.5.20902.9026.nupkg"), true);

    NuGetJavaFeedControllerIoC.setFeedProvider(myFeedProvider);
    startServer();
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    stopServer();
    super.tearDown();
  }

  @Test(enabled = false)
  public void startDebugServer() throws InterruptedException {
    System.out.println("-Source " + getNuGetServerUrl());
    //noinspection InfiniteLoopStatement
    while (true) {
      Thread.sleep(1000);
    }
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void testNuGetClientReadsFeed(final NugetFeedLibrary library, @NotNull final NuGet nuget) throws Exception {
    setODataSerializer(library);
    enableDebug();

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    nuget.makeOutputVerbose(cmd);
    cmd.addParameter("-AllVersions");
    cmd.addParameter("-Source");
    cmd.addParameter(getNuGetServerUrl());

    final ExecResult exec = SimpleCommandLineProcessRunner.runCommand(cmd, null);
    final String stdout = exec.getStdout();
    System.out.println(stdout);
    System.out.println(exec.getStderr());
    Assert.assertEquals(exec.getExitCode(), 0);
    Assert.assertTrue(stdout.contains(packageId_1), stdout);
    Assert.assertTrue(stdout.contains(packageId_2), stdout);
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void testNuGetClientReadsFeedQuery(final NugetFeedLibrary library, @NotNull final NuGet nuget) throws Exception {
    setODataSerializer(library);
    enableDebug();

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("Common");
    nuget.makeOutputVerbose(cmd);
    cmd.addParameter("-AllVersions");
    cmd.addParameter("-Source");
    cmd.addParameter(getNuGetServerUrl());

    final ExecResult exec = SimpleCommandLineProcessRunner.runCommand(cmd, null);
    Assert.assertEquals(exec.getExitCode(), 0);
    final String stdout = exec.getStdout();
    System.out.println(stdout);
    Assert.assertTrue(stdout.contains(packageId_1), stdout);
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void testNuGetClientBatchQueryForList(final NugetFeedLibrary library, @NotNull final NuGet nuget) throws Exception {
    setODataSerializer(library);
    enableDebug();

    String packageName = StringUtil.repeat("Common", " ", 256);
    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter(packageName);
    nuget.makeOutputVerbose(cmd);
    cmd.addParameter("-Source");
    cmd.addParameter(getNuGetServerUrl());

    final ExecResult exec = SimpleCommandLineProcessRunner.runCommand(cmd, null);
    Assert.assertEquals(exec.getExitCode(), 0);
    final String stdout = exec.getStdout();
    System.out.println(stdout);
    Assert.assertTrue(stdout.contains(packageId_1), stdout);
  }

  @TestFor(issues = "TW-24051")
  @Test(dataProvider = NUGET_VERSIONS)
  public void testNuGetClientReadsPrereleaseFeedQuery(final NugetFeedLibrary library, @NotNull final NuGet nuget) throws Exception {
    setODataSerializer(library);
    enableDebug();
    enablePackagesIndexSorting();

    addMockPackage("foo", "1.0.0");
    addMockPackage("foo", "2.0.0");
    addMockPackage("foo", "1.1.0");
    addMockPackage("foo", "2.1.0-alpha");

    dumpFeed();

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("-Prerelease");
    cmd.addParameter("-AllVersions");
    cmd.addParameter("-Source");
    cmd.addParameter(getNuGetServerUrl());

    final ExecResult exec = SimpleCommandLineProcessRunner.runCommand(cmd, null);
    Assert.assertEquals(exec.getExitCode(), 0, exec.getStderr());
    final String stdout = exec.getStdout();
    System.out.println(stdout);
    Assert.assertTrue(stdout.contains("foo 2.1.0-alpha"));
  }

  @Test(dataProvider = NUGET_VERSIONS_27p)
  public void testSkiptoken(final NugetFeedLibrary library, @NotNull final NuGet nuget) throws Exception {
    setODataSerializer(library);
    enableDebug();
    enablePackagesIndexSorting();

    int size = NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE + 2;
    for (int i = 0; i <= size; i++) {
      addMockPackage("skiptoken", "1.0." + i);
    }

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("skiptoken");
    cmd.addParameter("-AllVersions");
    cmd.addParameter("-Source");
    cmd.addParameter(getNuGetServerUrl());

    final ExecResult exec = SimpleCommandLineProcessRunner.runCommand(cmd, null);
    Assert.assertEquals(exec.getExitCode(), 0, exec.getStderr());
    final String stdout = exec.getStdout();
    System.out.println(stdout);

    for (int i = 0; i <= size; i++) {
      Assert.assertTrue(stdout.contains("skiptoken 1.0." + i));
    }
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_batch_reportNUnitAndYouTrackSharp_from_default_feed_x1(final NugetFeedLibrary library, @NotNull final NuGet nuget)
          throws NuGetExecutionException, IOException {
    setODataSerializer(library);
    doTriggerTest(nuget, packageId_1);
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_batch_reportNUnitAndYouTrackSharp_from_default_feed_x2(final NugetFeedLibrary library, @NotNull final NuGet nuget)
          throws NuGetExecutionException, IOException {
    setODataSerializer(library);
    doTriggerTest(nuget, packageId_1.toLowerCase(), packageId_2.toUpperCase());
  }

  private void doTriggerTest(@NotNull final NuGet nuget, @NotNull String... packageNames) throws NuGetExecutionException, IOException {
    ListPackagesCommand cmd = ListPackagesCommandIntegrationTest.createMockCommand(myNuGetTeamCityProvider, createTempDir());
    ListPackagesCommandIntegrationTest.doTriggerTest(cmd, nuget, getNuGetServerUrl(), packageNames);
  }

  private void startServer() throws Exception {
    myServerUrl = getServerURI();
    myServer = createHttpServer(myServerUrl);
    myServer.start();
  }

  private HttpServer createHttpServer(final URI serverURI) throws IOException {
    final ResourceConfig resourceConfig = new ClassNamesResourceConfig(NuGetJavaFeedTestController.class);
    return HttpServerFactory.create(serverURI, resourceConfig);
  }

  private static URI getServerURI() throws IOException {
    ServerSocket socket = null;
    try {
      socket = new ServerSocket(0);
      return UriBuilder.fromUri("http://localhost/").port(socket.getLocalPort()).build();
    } finally {
      if (socket != null) {
        socket.close();
      }
    }
  }

  private void stopServer() {
    if (myServer != null) {
      myServer.stop(0);
      myServer = null;
    }
  }

  @NotNull
  @Override
  protected Object[][] versionsFrom(@NotNull NuGet lowerBound) {
    Object[][] versions = super.versionsFrom(lowerBound);
    Object[][] objects = new Object[versions.length * 2][];
    for (int i = 0; i < versions.length; i++) {
      Object nuget = versions[i][0];
      objects[i * 2] = new Object[]{NugetFeedLibrary.OData4j, nuget};
      objects[i * 2 + 1] = new Object[]{NugetFeedLibrary.Olingo, nuget};
    }

    return objects;
  }
}
