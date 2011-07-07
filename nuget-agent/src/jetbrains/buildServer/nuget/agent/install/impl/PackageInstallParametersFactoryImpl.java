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

package jetbrains.buildServer.nuget.agent.install.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.install.PackageInstallParametersFactory;
import jetbrains.buildServer.nuget.agent.install.PackagesInstallParameters;
import jetbrains.buildServer.nuget.common.PackagesInstallerConstants;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 18:09
 */
public class PackageInstallParametersFactoryImpl implements PackageInstallParametersFactory {
  @NotNull
  public PackagesInstallParameters loadParameters(@NotNull final BuildRunnerContext context) throws RunBuildException {
    return new PackagesInstallParameters() {

      private File resolvePath(@Nullable final String runnerParameter) throws RunBuildException {
        String path = context.getRunnerParameters().get(runnerParameter);
        if (StringUtil.isEmptyOrSpaces(path))
          throw new RunBuildException("Runner parameter '" + runnerParameter + "' was not found");

        File file = FileUtil.resolvePath(context.getBuild().getCheckoutDirectory(), path);
        if (!file.exists()) {
          throw new RunBuildException("File does not exists: " + file);
        }

        return file;
      }


      @NotNull
      public File getSolutionFile() throws RunBuildException {
        return resolvePath(PackagesInstallerConstants.SLN_PATH);
      }

      @NotNull
      public File getNuGetExeFile() throws RunBuildException {
        return resolvePath(PackagesInstallerConstants.NUGET_PATH);
      }

      @NotNull
      public Collection<String> getNuGetPackageSources() {
        String sources = context.getRunnerParameters().get(PackagesInstallerConstants.NUGET_SOURCES);
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
    };
  }
}
