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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.01.12 0:40
 */
public class NuGetJavaFeedIntegrationTest extends NuGetJavaFeedIntegrationTestBase {

  @Test(dataProvider = NUGET_VERSIONS_15p)
  public void testNuGetClientReadsFeed(@NotNull final NuGet nuget) throws Exception{
    enableDebug();

    final String packageId_1 = "CommonServiceLocator";
    final String packageId_2 = "NuGet.Core";

    addPackage(Paths.getTestDataPath("/packages/" + packageId_1 + ".1.0.nupkg"));
    addPackage(Paths.getTestDataPath("/packages/" + packageId_2 + ".1.5.20902.9026.nupkg"));

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("-Source");
    cmd.addParameter(getEndpointUrl());

    final ExecResult exec = SimpleCommandLineProcessRunner.runCommand(cmd, null);
    Assert.assertEquals(exec.getExitCode(), 0);
    final String stdout = exec.getStdout();
    System.out.println(stdout);
    Assert.assertTrue(stdout.contains(packageId_1), stdout);
    Assert.assertTrue(stdout.contains(packageId_2), stdout);
  }

  @Test(dataProvider = NUGET_VERSIONS_15p)
  public void testNuGetClientReadsFeedQuery(@NotNull final NuGet nuget) throws Exception{
    enableDebug();

    final String packageId_1 = "CommonServiceLocator";
    final String packageId_2 = "NuGet.Core";

    addPackage(Paths.getTestDataPath("/packages/" + packageId_1 + ".1.0.nupkg"));
    addPackage(Paths.getTestDataPath("/packages/" + packageId_2 + ".1.5.20902.9026.nupkg"));

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("Common");
    cmd.addParameter("-Source");
    cmd.addParameter(getEndpointUrl());

    final ExecResult exec = SimpleCommandLineProcessRunner.runCommand(cmd, null);
    Assert.assertEquals(exec.getExitCode(), 0);
    final String stdout = exec.getStdout();
    System.out.println(stdout);
    Assert.assertTrue(stdout.contains(packageId_1), stdout);
    Assert.assertTrue(stdout.contains(packageId_2), stdout);
  }

}
