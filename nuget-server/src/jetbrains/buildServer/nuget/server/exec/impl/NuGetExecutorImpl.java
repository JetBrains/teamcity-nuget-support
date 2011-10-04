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

package jetbrains.buildServer.nuget.server.exec.impl;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutor;
import jetbrains.buildServer.nuget.server.exec.NuGetOutputProcessor;
import jetbrains.buildServer.nuget.server.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 12:48
 */
public class NuGetExecutorImpl implements NuGetExecutor {
  private static final Logger LOG = Logger.getInstance(NuGetExecutorImpl.class.getName());

  private final NuGetTeamCityProvider myNuGetTeamCityProvider;

  public NuGetExecutorImpl(@NotNull final NuGetTeamCityProvider nuGetTeamCityProvider) {
    myNuGetTeamCityProvider = nuGetTeamCityProvider;
  }

  @NotNull
  public <T> T executeNuGet(@NotNull final File nugetExePath,
                            @NotNull final List<String> arguments,
                            @NotNull final NuGetOutputProcessor<T> listener) throws NuGetExecutionException {

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(myNuGetTeamCityProvider.getNuGetRunnerPath().getPath());
    cmd.addParameter(nugetExePath.getPath());
    cmd.addParameters(arguments);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Starting: " + cmd.getCommandLineString());
    }

    final ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Exited with code: " + result.getExitCode());
      if (!StringUtil.isEmptyOrSpaces(result.getStdout())) {
        LOG.debug("Output: " + result.getStdout());
      }
      if (!StringUtil.isEmptyOrSpaces(result.getStderr())) {
        LOG.debug("Error: " + result.getStderr());
      }
    }

    listener.onStdOutput(result.getStdout());
    listener.onStdError(result.getStderr());
    listener.onFinished(result.getExitCode());

    return listener.getResult();
  }
}
