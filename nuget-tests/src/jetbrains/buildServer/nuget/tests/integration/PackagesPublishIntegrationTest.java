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

package jetbrains.buildServer.nuget.tests.integration;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.runner.publish.PackagesPublishRunner;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.07.11 1:25
 */
public class PackagesPublishIntegrationTest extends IntegrationTestBase {
  protected NuGetPublishParameters myPublishParameters;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myPublishParameters = m.mock(NuGetPublishParameters.class);
  }

  @Test
  public void test_publish_packages() throws IOException, RunBuildException {
    final File pkg = preparePackage();
    callPublishRunner(pkg);

    Assert.assertTrue(getCommandsOutput().contains("Your package was uploaded"));
  }

  @Test
  public void test_create_mock_package() throws IOException {
    final File file = preparePackage();
    System.out.println(file);
  }

  private File preparePackage() throws IOException {
    @NotNull final File root = createTempDir();
    final File spec = new File(root, "SamplePackage.nuspec");
    FileUtil.copy(getTestDataPath("SamplePackage.nuspec"), spec);

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(Paths.getPathToNuGet().getPath());
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
    cmd.addParameter("-Verbose");

    final ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);
    System.out.println(result.getStdout());
    System.out.println(result.getStderr());

    Assert.assertEquals(0, result.getExitCode());

    File pkg = new File(root, "SamplePackage." + build + ".nupkg");
    Assert.assertTrue(pkg.isFile());
    return pkg;
  }

  private void callPublishRunner(@NotNull final File pkg) throws RunBuildException {

    m.checking(new Expectations(){{
      allowing(myPublishParameters).getFiles(); will(returnValue(Arrays.asList(pkg.getPath())));
      allowing(myPublishParameters).getCreateOnly(); will(returnValue(true));
      allowing(myPublishParameters).getNuGetExeFile(); will(returnValue(Paths.getPathToNuGet()));
      allowing(myPublishParameters).getPublishSource(); will(returnValue(null));
      allowing(myPublishParameters).getApiKey(); will(returnValue(getQ()));

      allowing(myParametersFactory).loadPublishParameters(myContext);will(returnValue(myPublishParameters));
    }});

    final PackagesPublishRunner runner = new PackagesPublishRunner(myActionFactory, myParametersFactory);

    final BuildProcess proc = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);
  }

  private String getQ() {
    final int i1 = 88001628;
    final int universe = 42;
    final int num = 4015;
    final String nuget = 91 + "be" + "-" + num + "cf638bcf";
    return (i1 + "-" + "cb" + universe + "-" + 4 + "c") + 35 + "-" + nuget;
  }
}
