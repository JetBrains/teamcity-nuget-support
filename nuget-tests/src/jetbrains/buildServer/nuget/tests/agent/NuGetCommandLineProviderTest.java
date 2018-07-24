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

package jetbrains.buildServer.nuget.tests.agent;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.dotNet.DotNetConstants;
import jetbrains.buildServer.nuget.agent.util.CommandLineExecutor;
import jetbrains.buildServer.nuget.agent.util.impl.NuGetCommandLineProvider;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 08.12.11 15:34
 */
public class NuGetCommandLineProviderTest extends BaseTestCase {
  private static final String RUNNER_EXE = "runner.exe";
  private static final String MONO_PATH = "/usr/bin/mono-sgen";
  private Mockery m;
  private BuildRunnerContext myRootContext;
  private NuGetTeamCityProvider myNugetProvider;
  private File myWorkDir;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myWorkDir = createTempDir();
    myRootContext = m.mock(BuildRunnerContext.class);
    myNugetProvider = m.mock(NuGetTeamCityProvider.class);
    BuildParametersMap parametersMap = m.mock(BuildParametersMap.class);
    Map<String, String> configParameters = new HashMap<>();
    if(!SystemInfo.isWindows) {
      configParameters.put(DotNetConstants.MONO_JIT, MONO_PATH);
    }

    m.checking(new Expectations(){{
      allowing(myRootContext).getConfigParameters(); will(returnValue(configParameters));
      allowing(myRootContext).getBuildParameters(); will(returnValue(parametersMap));
      allowing(myRootContext).addEnvironmentVariable(with(any(String.class)), with(any(String.class)));
      allowing(myRootContext).getWorkingDirectory(); will(returnValue(myWorkDir));
      allowing(parametersMap).getEnvironmentVariables(); will(returnValue(Collections.emptyMap()));
      allowing(myNugetProvider).getNuGetRunnerPath(); will(returnValue(new File(RUNNER_EXE)));
    }});
  }

  @Test
  public void getCommandLine() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);
    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());

    args.add(0, executable);
    if (SystemInfo.isWindows) {
      Assert.assertEquals(commandLine.getExecutablePath(), RUNNER_EXE);
      Assert.assertEquals(commandLine.getArguments(), args);
    } else {
      Assert.assertEquals(commandLine.getExecutablePath(), MONO_PATH);
      args.add(0, executable);
      Assert.assertEquals(commandLine.getArguments(), args);
    }
  }
}
