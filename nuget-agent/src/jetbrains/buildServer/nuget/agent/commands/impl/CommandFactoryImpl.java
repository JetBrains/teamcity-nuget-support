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

package jetbrains.buildServer.nuget.agent.commands.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.commands.CommandFactory;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 16:10
 */
public class CommandFactoryImpl implements CommandFactory {

  @NotNull
  public <T> T createInstall(@NotNull PackagesInstallParameters params, @NotNull File packagesConfig, @NotNull File targetFolder, @NotNull Callback<T> factory) throws RunBuildException {
    final List<String> argz = new ArrayList<String>();
    argz.add("install");
    argz.add(FileUtil.getCanonicalFile(packagesConfig).getPath()); //path to package
    if (params.getExcludeVersion()) {
      argz.add("-ExcludeVersion");
    }
    argz.add("-OutputDirectory");
    argz.add(FileUtil.getCanonicalFile(targetFolder).getPath());

    return executeNuGet(params.getNuGetParameters(), argz, packagesConfig.getParentFile(), factory);
  }

  @NotNull
  public <T> T createUpdate(@NotNull PackagesUpdateParameters params, @NotNull File packagesConfig, @NotNull File targetFolder, @NotNull Callback<T> factory) throws RunBuildException {
    final List<String> argz = new ArrayList<String>();
    argz.add("update");
    argz.add(FileUtil.getCanonicalFile(packagesConfig).getPath()); //path to package
    if (params.getUseSafeUpdate()) {
      argz.add("-Safe");
    }
    argz.add("-Verbose");
    argz.add("-RepositoryPath");
    argz.add(FileUtil.getCanonicalFile(targetFolder).getPath());

    for (String id : params.getPackagesToUpdate()) {
      argz.add("-Id");
      argz.add(id);
    }

    return executeNuGet(params.getNuGetParameters(), argz, packagesConfig.getParentFile(), factory);
  }

  @NotNull
  public <T> T createPush(@NotNull final NuGetPublishParameters params,
                          @NotNull final File packagePath,
                          @NotNull final Callback<T> factory) throws RunBuildException {
    final List<String> arguments = new ArrayList<String>();
    arguments.add(packagePath.getPath());
    arguments.add(params.getApiKey());
    if (params.getCreateOnly()) {
      arguments.add("-CreateOnly");
    }

    return executeNuGet(
            params,
            Arrays.asList(params.getPublishSource()),
            arguments,
            packagePath.getParentFile(),
            factory);
  }

  @NotNull
  private <T> T executeNuGet(@NotNull final NuGetFetchParameters nuget,
                             @NotNull final Collection<String> arguments,
                             @NotNull final File workingDir,
                             @NotNull final Callback<T> factory) throws RunBuildException {
    return executeNuGet(nuget, nuget.getNuGetPackageSources(), arguments, workingDir, factory);
  }

  private <T> T executeNuGet(NuGetParameters nuget, Collection<String> sources, Collection<String> arguments, File workingDir, Callback<T> factory) throws RunBuildException {
    final List<String> argz = new ArrayList<String>(arguments);
    for (String source : sources) {
      argz.add("-Source");
      argz.add(source);
    }

    return factory.createCommand(
            nuget.getNuGetExeFile(),
            workingDir,
            argz
    );
  }

}
