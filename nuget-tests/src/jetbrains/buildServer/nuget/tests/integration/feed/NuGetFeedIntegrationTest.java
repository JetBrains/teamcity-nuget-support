/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.feed;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.nuget.tests.integration.ProcessRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.09.11 20:33
 */
public class NuGetFeedIntegrationTest extends NuGetFeedIntegrationTestBase {

  @Test
  public void test_list_all() throws IOException {
    final NuGet nuget = NuGet.NuGet_1_5;
    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("-Source");
    cmd.addParameter(myFeedUrl);

    final ExecResult execResult = ProcessRunner.runProces(cmd);
    Assert.assertEquals(execResult.getExitCode(), 0, "Exit code must be 0");
  }

  @Test
  public void test_list_name() throws IOException {
    final NuGet nuget = NuGet.NuGet_1_5;
    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("jpkg");
    cmd.addParameter("-Source");
    cmd.addParameter(myFeedUrl);

    final ExecResult execResult = ProcessRunner.runProces(cmd);
    Assert.assertEquals(execResult.getExitCode(), 0, "Exit code must be 0");
  }

  @Test
  public void test_list_versions() throws IOException {
    final NuGet nuget = NuGet.NuGet_1_5;
    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("jpkg");
    cmd.addParameter("-AllVersions");
    cmd.addParameter("-Source");
    cmd.addParameter(myFeedUrl);

    final ExecResult execResult = ProcessRunner.runProces(cmd);
    Assert.assertEquals(execResult.getExitCode(), 0, "Exit code must be 0");
  }

  @Test
  public void test_install_any() throws IOException {
    final NuGet nuget = NuGet.NuGet_1_5;
    final File temp = createTempDir();

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("install");
    cmd.addParameter("jpkg");
    cmd.addParameter("-Source");
    cmd.addParameter(myFeedUrl);
    cmd.addParameter("-OutputDirectory");
    cmd.addParameter(temp.getPath());

    final ExecResult execResult = ProcessRunner.runProces(cmd);
    Assert.assertEquals(execResult.getExitCode(), 0, "Exit code must be 0");
  }

  @Test
  public void test_install_version() throws IOException {
    final NuGet nuget = NuGet.NuGet_1_5;
    final File temp = createTempDir();

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nuget.getPath().getPath());
    cmd.addParameter("install");
    cmd.addParameter("jpkg");
    cmd.addParameter("-Version");
    cmd.addParameter("5.4.32");
    cmd.addParameter("-Source");
    cmd.addParameter(myFeedUrl);
    cmd.addParameter("-OutputDirectory");
    cmd.addParameter(temp.getPath());

    final ExecResult execResult = ProcessRunner.runProces(cmd);
    Assert.assertEquals(execResult.getExitCode(), 0, "Exit code must be 0");
  }

}
