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
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.impl.CommandFactoryImpl;
import jetbrains.buildServer.nuget.agent.commands.impl.LoggingNuGetActionFactoryImpl;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetActionFactoryImpl;
import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorImpl;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesConfigParser;
import jetbrains.buildServer.nuget.agent.dependencies.impl.PackageUsagesImpl;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.07.11 1:26
 */
public class IntegrationTestBase extends BuildProcessTestCase {
  private StringBuilder myCommandsOutput;
  protected File myRoot;
  protected Mockery m;
  protected AgentRunningBuild myBuild;
  protected BuildRunnerContext myContext;
  protected BuildProgressLogger myLogger;
  protected PackagesParametersFactory myParametersFactory;
  protected NuGetFetchParameters myNuGet;
  protected NuGetPackagesCollector myCollector;
  protected NuGetActionFactory myActionFactory;
  private BuildProcess myMockProcess;
  protected BuildParametersMap myBuildParametersMap;
  protected String cmd;

  @NotNull
  protected String getCommandsOutput() {
    return myCommandsOutput.toString();
  }


  public static final String NUGET_VERSIONS = "nuget_versions";

  @DataProvider(name = NUGET_VERSIONS)
  public Object[][] dataProviderNuGetVersions() {
    return new Object[][]{
            new Object[] { NuGet.NuGet_1_4},
            new Object[] { NuGet.NuGet_1_5},
    };
  }

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCommandsOutput = new StringBuilder();
    myRoot = createTempDir();
    m = new Mockery();
    myBuild = m.mock(AgentRunningBuild.class);
    myContext = m.mock(BuildRunnerContext.class);
    myLogger = m.mock(BuildProgressLogger.class);
    myParametersFactory = m.mock(PackagesParametersFactory.class);
    myMockProcess = m.mock(BuildProcess.class);
    myNuGet = m.mock(NuGetFetchParameters.class);
    myBuildParametersMap = m.mock(BuildParametersMap.class);

    cmd = System.getenv("ComSpec");

    m.checking(new Expectations(){{
      allowing(myContext).getBuildParameters(); will(returnValue(myBuildParametersMap));
      allowing(myBuildParametersMap).getEnvironmentVariables(); will(returnValue(Collections.singletonMap("ComSpec", cmd)));

      allowing(myContext).getBuild();
      will(returnValue(myBuild));
      allowing(myBuild).getBuildLogger();
      will(returnValue(myLogger));
      allowing(myBuild).getCheckoutDirectory();
      will(returnValue(myRoot));

      allowing(myMockProcess).start();
      allowing(myMockProcess).waitFor();
      will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      allowing(myLogger).message(with(any(String.class)));
    }});

    myCollector = new NuGetPackagesCollectorImpl();
    PackageUsages pu = new PackageUsagesImpl(
            myCollector,
            new NuGetPackagesConfigParser()
    );

    myActionFactory = new LoggingNuGetActionFactoryImpl(new NuGetActionFactoryImpl(executingFactory(), pu, new CommandFactoryImpl()));
  }

  @NotNull
  protected File getTestDataPath(final String path) {
    return Paths.getTestDataPath("integration/" + path);
  }

  @NotNull
  private CommandlineBuildProcessFactory executingFactory() {
    return new CommandlineBuildProcessFactory() {
      @NotNull
      public BuildProcess executeCommandLine(@NotNull final BuildRunnerContext hostContext,
                                             @NotNull final String program,
                                             @NotNull final Collection<String> argz,
                                             @NotNull final File workingDir,
                                             @NotNull final Map<String, String> additionalEnvironment) throws RunBuildException {
        return new BuildProcessBase() {
          @NotNull
          @Override
          protected BuildFinishedStatus waitForImpl() throws RunBuildException {
            GeneralCommandLine cmd = new GeneralCommandLine();
            cmd.setExePath(program);
            for (String arg : argz) {
              cmd.addParameter(arg.replaceAll("%+", "%"));
            }
            cmd.setWorkingDirectory(workingDir);

            Map<String, String> env = new HashMap<String, String>();
            env.putAll(System.getenv());
            env.putAll(additionalEnvironment);
            cmd.setEnvParams(env);

            final ExecResult result = ProcessRunner.runProces(cmd);
            myCommandsOutput.append(result.getStdout()).append("\n\n").append(result.getStderr()).append("\n\n");

            return result.getExitCode() == 0
                    ? BuildFinishedStatus.FINISHED_SUCCESS
                    : BuildFinishedStatus.FINISHED_FAILED;
          }
        };
      }
    };
  }
}
