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
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.util.Collection;

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
  protected PackagesInstallParameters myInstall;
  protected PackagesUpdateParameters myUpdate;
  protected NuGetPublishParameters myPublishParameters;
  protected NuGetFetchParameters myNuGet;
  private BuildProcess myMockProcess;

  @NotNull
  protected String getCommandsOutput() {
    return myCommandsOutput.toString();
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
    myInstall = m.mock(PackagesInstallParameters.class);
    myUpdate = m.mock(PackagesUpdateParameters.class);
    myMockProcess = m.mock(BuildProcess.class);
    myNuGet = m.mock(NuGetFetchParameters.class);
    myPublishParameters = m.mock(NuGetPublishParameters.class);

    m.checking(new Expectations() {{
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
      allowing(myLogger).activityStarted(with(equal("install")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("install")), with(any(String.class)));

      allowing(myInstall).getNuGetParameters();
      will(returnValue(myNuGet));
      allowing(myUpdate).getNuGetParameters();
      will(returnValue(myNuGet));
    }});
  }

  @NotNull
  protected File getTestDataPath(final String path) {
    return Paths.getTestDataPath("integration/" + path);
  }

  protected CommandlineBuildProcessFactory executingFactory() {
    return new CommandlineBuildProcessFactory() {
      public BuildProcess executeCommandLine(@NotNull final BuildRunnerContext hostContext,
                                             @NotNull final File program,
                                             @NotNull final Collection<String> argz,
                                             @NotNull final File workingDir) throws RunBuildException {
        return new BuildProcessBase() {
          @NotNull
          @Override
          protected BuildFinishedStatus waitForImpl() throws RunBuildException {
            GeneralCommandLine cmd = new GeneralCommandLine();
            cmd.setExePath(program.getPath());
            for (String arg : argz) {
              cmd.addParameter(arg);
            }
            cmd.setWorkingDirectory(workingDir);

            System.out.println("Run: " + cmd.getCommandLineString());

            ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);

            System.out.println(result.getStdout());
            System.out.println(result.getStderr());

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
