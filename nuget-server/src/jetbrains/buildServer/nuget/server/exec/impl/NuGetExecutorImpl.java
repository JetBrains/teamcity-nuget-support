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

package jetbrains.buildServer.nuget.server.exec.impl;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.TempFolderProvider;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.nuget.common.auth.PackageSourceUtil;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutor;
import jetbrains.buildServer.nuget.server.exec.NuGetOutputProcessor;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.nuget.server.util.TempFilesUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.nuget.common.auth.NuGetAuthConstants.NUGET_CREDENTIALPROVIDERS_PATH_ENV_VAR;
import static jetbrains.buildServer.nuget.common.auth.NuGetAuthConstants.TEAMCITY_NUGET_FEEDS_ENV_VAR;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 12:48
 */
public class NuGetExecutorImpl implements NuGetExecutor {
  private static final Logger LOG = Logger.getInstance(NuGetExecutorImpl.class.getName());

  @NotNull private final SystemInfo mySystemInfo;
  @NotNull private final TempFolderProvider myTempFiles;
  @NotNull private final NuGetTeamCityProvider myNuGetTeamCityProvider;

  public NuGetExecutorImpl(@NotNull final NuGetTeamCityProvider nuGetTeamCityProvider,
                           @NotNull final SystemInfo systemInfo,
                           @NotNull final TempFolderProvider tempFiles) {
    myNuGetTeamCityProvider = nuGetTeamCityProvider;
    mySystemInfo = systemInfo;
    myTempFiles = tempFiles;
  }

  @NotNull
  public <T> T executeNuGet(@NotNull final File nugetExePath,
                            @NotNull final List<String> arguments,
                            @NotNull final Collection<PackageSource> packageSources,
                            @NotNull final NuGetOutputProcessor<T> listener) throws NuGetExecutionException {
    assertOs();

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(myNuGetTeamCityProvider.getNuGetRunnerPath().getAbsolutePath());
    cmd.addParameter(nugetExePath.getPath());
    cmd.addParameters(arguments);


    File sourcesFile;
    try {
      sourcesFile = TempFilesUtil.createTempFile(myTempFiles.getTempDirectory(), "trigger-sources");
      PackageSourceUtil.writeSources(sourcesFile, packageSources);
    } catch (IOException e) {
      throw new NuGetExecutionException("Failed to create temp file for NuGet sources. " + e.getMessage(), e);
    }

    final Map<String, String> additionalEnvironment = new HashMap<String, String>();
    additionalEnvironment.put(TEAMCITY_NUGET_FEEDS_ENV_VAR, sourcesFile.getAbsolutePath());
    additionalEnvironment.put(NUGET_CREDENTIALPROVIDERS_PATH_ENV_VAR, myNuGetTeamCityProvider.getCredentialProviderHomeDirectory().getAbsolutePath());
    cmd.setEnvParams(additionalEnvironment);
    cmd.setPassParentEnvs(true);

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

    FileUtil.delete(sourcesFile);

    listener.onStdOutput(result.getStdout());
    listener.onStdError(result.getStderr());
    listener.onFinished(result.getExitCode());

    return listener.getResult();
  }


  private void assertOs() throws NuGetExecutionException {
    if (!mySystemInfo.canStartNuGetProcesses()) {
      throw new NuGetExecutionException("Starting of nuget.exe processes is not supported by current environment.");
    }
  }

}
