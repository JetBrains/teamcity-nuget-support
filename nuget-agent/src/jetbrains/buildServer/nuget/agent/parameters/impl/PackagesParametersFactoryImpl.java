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

package jetbrains.buildServer.nuget.agent.parameters.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static jetbrains.buildServer.nuget.common.PackagesConstants.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 18:09
 */
public class PackagesParametersFactoryImpl implements PackagesParametersFactory {
  @NotNull
  public PackagesInstallParameters loadParameters(@NotNull final BuildRunnerContext context) throws RunBuildException {
    return new PackagesInstallParameters() {

      private File resolvePath(@Nullable final String runnerParameter, @NotNull String name) throws RunBuildException {
        String path = getParameter(runnerParameter);
        if (StringUtil.isEmptyOrSpaces(path))
          throw new RunBuildException("Runner parameter '" + runnerParameter + "' was not found");

        File file = FileUtil.resolvePath(context.getBuild().getCheckoutDirectory(), path);
        if (!file.exists()) {
          throw new RunBuildException("Failed to find " + name + " at " + file);
        }

        return file;
      }


      @NotNull
      public File getSolutionFile() throws RunBuildException {
        return resolvePath(SLN_PATH, "Visual Studio .sln file");
      }

      @NotNull
      public File getNuGetExeFile() throws RunBuildException {
        return resolvePath(NUGET_PATH, "nuget.exe");
      }

      @NotNull
      public Collection<String> getNuGetPackageSources() {
        String sources = getParameter(NUGET_SOURCES);
        if (sources == null) return Collections.emptyList();

        List<String> list = new ArrayList<String>();
        for (String _source : sources.split("[\\r\\n]+")) {
          final String source = _source.trim();
          if (!source.isEmpty()) {
            list.add(source);
          }
        }

        return Collections.unmodifiableList(list);
      }

      public boolean getExcludeVersion() {
        return getBoolean(NUGET_EXCLUDE_VERSION);
      }

      private boolean getBoolean(String key) {
        return !StringUtil.isEmptyOrSpaces(getParameter(key));
      }

      private String getParameter(String key) {
        return context.getRunnerParameters().get(key);
      }
    };
  }
}
