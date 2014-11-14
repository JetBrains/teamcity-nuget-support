/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.tests.integration.ListPackagesCommandIntegrationTest;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.01.12 0:40
 */
public class NuGetJavaFeedIntegrationTest extends NuGetJavaFeedIntegrationTestBase {
  private final String packageId_1 = "CommonServiceLocator";
  private final String packageId_2 = "NuGet.Core";

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    addPackage(Paths.getTestDataPath("/packages/" + packageId_1 + ".1.0.nupkg"), true);
    addPackage(Paths.getTestDataPath("/packages/" + packageId_2 + ".1.5.20902.9026.nupkg"), true);
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
  public void testNuGetClientReadsFeed(@NotNull final NuGet nuget) throws Exception{
    enableDebug();

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("-Verbose");
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
  public void testNuGetClientReadsFeedQuery(@NotNull final NuGet nuget) throws Exception{
    enableDebug();

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("Common");
    cmd.addParameter("-Verbose");
    cmd.addParameter("-AllVersions");
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
  public void testNuGetClientReadsPrereleaseFeedQuery(@NotNull final NuGet nuget) throws Exception{
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

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_batch_reportNUnitAndYouTrackSharp_from_default_feed_x1(@NotNull final NuGet nuget) throws NuGetExecutionException, IOException {
    doTriggerTest(nuget, packageId_1);
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_batch_reportNUnitAndYouTrackSharp_from_default_feed_x2(@NotNull final NuGet nuget) throws NuGetExecutionException, IOException {
    doTriggerTest(nuget, packageId_1.toLowerCase(), packageId_2.toUpperCase());
  }

  protected void doTriggerTest(@NotNull final NuGet nuget, @NotNull String... packageNames) throws NuGetExecutionException, IOException {
    ListPackagesCommand cmd = ListPackagesCommandIntegrationTest.createMockCommand(myNuGetTeamCityProvider, createTempDir());
    ListPackagesCommandIntegrationTest.doTriggerTest(cmd, nuget, getNuGetServerUrl(), packageNames);
  }

}
