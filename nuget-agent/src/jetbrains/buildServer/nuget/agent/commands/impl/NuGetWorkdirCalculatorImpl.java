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

package jetbrains.buildServer.nuget.agent.commands.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 13:34
 */
public class NuGetWorkdirCalculatorImpl implements NuGetWorkdirCalculator {
  private static final Logger LOG = Logger.getInstance(NuGetWorkdirCalculatorImpl.class.getName());
  public static final String KEY = "teamcity.nuget.workdir.local";

  @Nullable 
  public File getNuGetWorkDir(@NotNull AgentRunningBuild build) {
    //compatibility with older behavior
    if ("true".equalsIgnoreCase(build.getSharedConfigParameters().get(KEY))) {
      LOG.warn("System wide NuGet config will be used because config parameter '" + KEY + "' was set");
      return null;
    }

    return new File(build.getAgentTempDirectory(), "TC.NuGet-" + build.getBuildId());
  }

  @NotNull
  public File getNuGetWorkDir(@NotNull final BuildRunnerContext context,
                              @NotNull final File defaultDir) throws RunBuildException {
    return getNuGetWorkDir(context.getBuild(), defaultDir);
  }
  
  @NotNull 
  private File getNuGetWorkDir(@NotNull final AgentRunningBuild build,
                              @NotNull final File defaultDir) throws RunBuildException {
    File workDir = getNuGetWorkDir(build);
    if (workDir == null) {
      
      return defaultDir;
    }
    
    updateNuGetConfig(build, workDir);
    return workDir;
  }

  private File updateNuGetConfig(@NotNull final AgentRunningBuild build,
                                 @NotNull final File workDir) throws RunBuildException {
    try {
      FileUtil.createDir(workDir);
    } catch (IOException e) {
      LOG.warn("Failed to create directory: " + workDir +". " + e.getMessage(), e);
      throw new RunBuildException("Failed to create directory: " + workDir +". " + e.getMessage());
    }

    //TODO: may be we should not rewrite existing config here
    //TODO: if should check .nuget folder to reuse nuget.config from it.
    final File localNuGetConfig = new File(workDir, "NuGet.Config");
    final File sharedNuGetConfig = getGlobalCongig(build);

    if (!localNuGetConfig.isFile()) {
      try {
        if (sharedNuGetConfig != null) {
          LOG.debug("Copy NuGet config from: " + sharedNuGetConfig + " to: " + localNuGetConfig);
          FileUtil.copy(sharedNuGetConfig, localNuGetConfig);
        } else {
          LOG.debug("Create empty NuGet config at: " + localNuGetConfig);
          saveUTForThrow(localNuGetConfig, "<?xml version=\"1.0\" encoding=\"utf-8\"?> <configuration/>");
        }
      } catch (IOException e) {
        LOG.warn("Failed create file: " + localNuGetConfig +". " + e.getMessage(), e);
        throw new RunBuildException("Failed create file: " + localNuGetConfig +". " + e.getMessage());
      }
    }
    return workDir;
  }

  @Nullable
  private File getGlobalCongig(@NotNull final AgentRunningBuild context) {
    final String appdata = context.getSharedBuildParameters().getEnvironmentVariables().get("APPDATA");
    if (StringUtil.isEmptyOrSpaces(appdata)) return null;
    final File data = new File(appdata);
    if (!data.isDirectory()) return null;

    final File home = new File(data, "NuGet/NuGet.config");
    return home.isFile() ? home : null;
  }

  private static void saveUTForThrow(@NotNull File file, @NotNull String text) throws IOException {
    Writer fis = null;
    try {
      fis = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), "UTF-8");
      fis.write(text);
      fis.flush();
    } catch (final IOException e) {
      throw new IOException("Failed to write " + file + ". " + e.getMessage()) {{initCause(e);}};
    } finally {
      FileUtil.close(fis);
    }
  }
}
