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

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.TestNGUtil;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.impl.AgentEventDispatcher;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.runner.NuGetCredentialsProvider;
import jetbrains.buildServer.nuget.agent.runner.publish.PackagesPublishRunner;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.07.11 1:25
 */
public class PackagesPublishIntegrationTest extends IntegrationTestBase {
  public static final ProtocolVersion PROTOCOL_VERSION = new ProtocolVersion("HTTP", 1, 1);
  protected NuGetPublishParameters myPublishParameters;
  private AgentEventDispatcher myEventDispatcher;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myPublishParameters = m.mock(NuGetPublishParameters.class);
    m.checking(new Expectations(){{
      allowing(myLogger).activityStarted(with(equal("push")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("push")), with(any(String.class)));
      allowing(myContext).getRunType();
      will(returnValue(PackagesConstants.PUBLISH_RUN_TYPE));
    }});
    myEventDispatcher = new AgentEventDispatcher();
    final NuGetCredentialsProvider provider = new NuGetCredentialsProvider(
      myEventDispatcher, myPsm, myNuGetTeamCityProvider
    );
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    myEventDispatcher.getMulticaster().runnerFinished(myContext, BuildFinishedStatus.FINISHED_SUCCESS);
  }

  @Test(dataProvider = NUGET_VERSIONS_18p)
  public void test_publish_wrong_files(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    final File home = createTempDir();
    final File pkg1 = new File(home, "a.b.c.4.3.zpoo"){{createNewFile(); }};
    final BuildProcess p = callPublishRunnerEx(nuget, pkg1);
    assertRunSuccessfully(p, BuildFinishedStatus.FINISHED_FAILED);

    Assert.assertTrue(getCommandsWarnings().contains(".zpoo"));

    m.assertIsSatisfied();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_publish_packages(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    TestNGUtil.skip("this test will publish a package to preview nuget repo");
    final File pkg = preparePackage(nuget);
    callPublishRunner(nuget, pkg);

    Assert.assertTrue(getCommandsOutput().contains("Your package was uploaded") || getCommandsOutput().contains("Your package was pushed."));
  }


  @Test(dataProvider = NUGET_VERSIONS_20p)
  public void test_publish_packages_mock_http(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    if (!SystemInfo.isWindows && nuget == NuGet.NuGet_3_3) {
      TestNGUtil.skip("NuGet 3.3 on Mono has problems with pack command");
    }

    final AtomicBoolean hasPUT = new AtomicBoolean();
    final SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(3000).build();
    ServerBootstrap bootstrap = ServerBootstrap.bootstrap().setSocketConfig(socketConfig).setServerInfo("TEST/1.1")
            .registerHandler("/*", new HttpRequestHandler() {
              @Override
              public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
                if (httpRequest.getRequestLine().getMethod().equals("PUT")) {
                  hasPUT.set(true);
                  if (httpRequest.containsHeader("Expect: 100-continue"))
                    httpResponse.setStatusLine(new BasicStatusLine(PROTOCOL_VERSION, 100, "Continue"));
                  else
                    httpResponse.setStatusLine(new BasicStatusLine(PROTOCOL_VERSION, 201, "Created"));
                } else
                  httpResponse.setStatusLine(new BasicStatusLine(PROTOCOL_VERSION, 200, "Ok"));
              }
            });

    HttpServer server = bootstrap.create();
    server.start();
    try {
      final File pkg = preparePackage(nuget);
      BuildProcess p = callPublishRunnerEx(nuget, "http://localhost:" + server.getLocalPort() + "/nuget", pkg);
      assertRunSuccessfully(p, BuildFinishedStatus.FINISHED_SUCCESS);

      Assert.assertTrue(getCommandsOutput().contains("Your package was uploaded") || getCommandsOutput().contains("Your package was pushed."));
      Assert.assertTrue(hasPUT.get());
    } finally {
      server.stop();
    }
  }

  @Test(dataProvider = NUGET_VERSIONS_20p)
  public void test_publish_packages_mock_http_auth(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    if (!SystemInfo.isWindows) {
      if (nuget == NuGet.NuGet_3_3) {
        TestNGUtil.skip("NuGet 3.3 on Mono has problems with pack command");
      }
      if (nuget == NuGet.NuGet_4_8) {
        TestNGUtil.skip("Credentials plugin in NuGet 4.8 does not work on Mono");
      }
    }

    final String username = "u-" + StringUtil.generateUniqueHash();
    final String password = "p-" + StringUtil.generateUniqueHash();
    final AtomicBoolean hasPUT = new AtomicBoolean();

    final SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(3000).build();
    ServerBootstrap bootstrap = ServerBootstrap.bootstrap().setSocketConfig(socketConfig).setServerInfo("TEST/1.1")
            .registerHandler("/*", new HttpRequestHandler() {
              @Override
              public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
                final Header authHeader = httpRequest.getFirstHeader("Authorization");
                if (authHeader != null) {
                  final String auth = authHeader.getValue();
                  if (auth.startsWith("Basic")) {
                    try {
                      final String encoded = auth.substring("Basic".length()).trim();
                      final String up = new String(new Base64().decode(encoded.getBytes("utf-8")), "utf-8");
                      System.out.println("Login with: " + up);
                      if ((username + ":" + password).equals(up)) {
                        handleAuth(httpRequest, httpResponse);
                        return;
                      }
                    } catch (IOException e) {
                      e.printStackTrace();
                      handleServerError(httpResponse);
                      return;
                    }
                  }
                }
                handleNotAuth(httpResponse);
              }

              private void handleServerError(HttpResponse httpResponse) {
                httpResponse.setStatusCode(500);
              }

              private void handleAuth(HttpRequest httpRequest, HttpResponse httpResponse) {
                if (httpRequest.getRequestLine().getMethod().equals("PUT")) {
                  hasPUT.set(true);
                  if(httpRequest.containsHeader("Expect: 100-continue"))
                    httpResponse.setStatusLine(new BasicStatusLine(PROTOCOL_VERSION, 100, "Continue"));
                  else
                    httpResponse.setStatusLine(new BasicStatusLine(PROTOCOL_VERSION, 201, "Created"));
                } else
                  httpResponse.setStatusLine(new BasicStatusLine(PROTOCOL_VERSION, 200, "Ok"));
              }

              private void handleNotAuth(HttpResponse httpResponse) {
                httpResponse.setStatusLine(new BasicStatusLine(PROTOCOL_VERSION, 401, "Authorization Required"));
                httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"Secure Area\"");
                httpResponse.setHeader("Content-Type", "text/plain");
                final BasicHttpEntity entity = new BasicHttpEntity();
                entity.setContentLength(0);
                entity.setContent(new ByteArrayInputStream("Authentication is required".getBytes()));
                httpResponse.setEntity(entity);
              }
            });

    HttpServer server = bootstrap.create();
    server.start();
    final String feed = "http://localhost:" + server.getLocalPort() + "/nuget";

    addGlobalSource(feed, username, password);
    try {
      final File pkg = preparePackage(nuget);

      BuildProcess p = callPublishRunnerEx(nuget, feed, pkg);
      assertRunSuccessfully(p, BuildFinishedStatus.FINISHED_SUCCESS);

      Assert.assertTrue(getCommandsOutput().contains("Your package was uploaded") || getCommandsOutput().contains("Your package was pushed."));
      Assert.assertTrue(hasPUT.get());
    } finally {
      server.stop();
    }
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_create_mock_package(@NotNull final NuGet nuget) throws IOException {
    final File file = preparePackage(nuget);
    System.out.println(file);
  }

  private File preparePackage(@NotNull final NuGet nuget) throws IOException {
    @NotNull final File root = createTempDir();
    final File spec = new File(root, "SamplePackage.nuspec");
    FileUtil.copy(getTestDataPath("SamplePackage.nuspec"), spec);

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.setWorkingDirectory(root);
    cmd.addParameter("pack");
    cmd.addParameter(spec.getPath());
    cmd.addParameter("-Version");
    long time = System.currentTimeMillis();
    final long max = 65536;
    String build = "";
    for(int i = 0; i <4; i++) {
      build = (Math.max(1, time % max)) + (build.length() == 0 ? "" : "." + build);
      time /= max;
    }
    cmd.addParameter(build);
    nuget.makeOutputVerbose(cmd);

    final ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);
    System.out.println(result.getStdout());
    System.out.println(result.getStderr());

    Assert.assertEquals(0, result.getExitCode(), result.getStderr());

    File pkg = new File(root, "jonnyzzz.nuget.teamcity.testPackage." + build + ".nupkg");
    Assert.assertTrue(pkg.isFile());
    return pkg;
  }

  private BuildProcess callPublishRunnerEx(@NotNull final NuGet nuget,
                                           @NotNull final File... pkg) throws RunBuildException {
    return callPublishRunnerEx(nuget, "http://nuget.org/api/v2", pkg);
  }

  private BuildProcess callPublishRunnerEx(@NotNull final NuGet nuget,
                                           @NotNull final String source,
                                           @NotNull final File... pkg) throws RunBuildException {
    final List<String> files = new ArrayList<String>();
    for (File p : pkg) {
      files.add(p.getPath());
    }
    m.checking(new Expectations(){{
      allowing(myPublishParameters).getFiles(); will(returnValue(files));
      allowing(myPublishParameters).getNuGetExeFile(); will(returnValue(nuget.getPath()));
      allowing(myPublishParameters).getPublishSource(); will(returnValue(source));
      allowing(myPublishParameters).getApiKey(); will(returnValue(getQ()));
      allowing(myPublishParameters).getCustomCommandline(); will(returnValue(Collections.emptyList()));

      allowing(myParametersFactory).loadPublishParameters(myContext);will(returnValue(myPublishParameters));
    }});

    final CompositeBuildProcessImpl buildProcess = new CompositeBuildProcessImpl();
    buildProcess.pushBuildProcess(new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        myEventDispatcher.getMulticaster().beforeRunnerStart(myContext);
        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    });
    buildProcess.pushBuildProcess(new PackagesPublishRunner(myActionFactory, myParametersFactory).createBuildProcess(myBuild, myContext));
    return buildProcess;
  }

  private void callPublishRunner(@NotNull final NuGet nuget, @NotNull final File pkg) throws RunBuildException {
    final BuildProcess proc = callPublishRunnerEx(nuget, pkg);
    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);
  }

  @NotNull
  private String getQ() {
    final int i1 = 88001628;
    final int universe = 42;
    final int num = 4015;
    final String nuget = 91 + "be" + "-" + num + "cf638bcf";
    return (i1 + "-" + "cb" + universe + "-" + 4 + "c") + 35 + "-" + nuget;
  }
}
