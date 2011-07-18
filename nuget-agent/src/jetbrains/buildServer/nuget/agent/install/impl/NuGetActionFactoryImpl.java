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
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.install.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.install.PackageUsages;
import jetbrains.buildServer.nuget.agent.parameters.NuGetParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 17:49
 */
public class NuGetActionFactoryImpl implements NuGetActionFactory {
  private final CommandlineBuildProcessFactory myFactory;
  private final PackageUsages myPackageUsages;

  public NuGetActionFactoryImpl(@NotNull final CommandlineBuildProcessFactory factory,
                                @NotNull final PackageUsages packageUsages) {
    myFactory = factory;
    myPackageUsages = packageUsages;
  }

  @NotNull
  public BuildProcess createInstall(@NotNull final BuildRunnerContext context,
                                    @NotNull final PackagesInstallParameters params,
                                    @NotNull final File packagesConfig,
                                    @NotNull final File targetFolder) throws RunBuildException {
    final List<String> argz = new ArrayList<String>();
    argz.add("install");
    argz.add(FileUtil.getCanonicalFile(packagesConfig).getPath()); //path to package
    if (params.getExcludeVersion()) {
      argz.add("-ExcludeVersion");
    }
    argz.add("-OutputDirectory");
    argz.add(FileUtil.getCanonicalFile(targetFolder).getPath());

    return executeNuGet(context, params.getNuGetParameters(), argz, packagesConfig.getParentFile());
  }


  @NotNull
  public BuildProcess createUpdate(@NotNull final BuildRunnerContext context,
                                   @NotNull final PackagesUpdateParameters params,
                                   @NotNull final File packagesConfig,
                                   @NotNull final File targetFolder) throws RunBuildException {
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

    return executeNuGet(context, params.getNuGetParameters(), argz, packagesConfig.getParentFile());
  }

  @NotNull
  public BuildProcess createUsageReport(@NotNull BuildRunnerContext context,
                                        @NotNull NuGetParameters params,
                                        @NotNull File packagesConfig,
                                        @NotNull File targetFolder) throws RunBuildException {
    return myPackageUsages.createReport(packagesConfig);
  }

  @NotNull
  private BuildProcess executeNuGet(@NotNull final BuildRunnerContext context,
                                    @NotNull final NuGetParameters nuget,
                                    @NotNull final Collection<String> arguments,
                                    @NotNull final File workingDirectory) throws RunBuildException {
    final List<String> argz = new ArrayList<String>(arguments);
    for (String source : nuget.getNuGetPackageSources()) {
      argz.add("-Source");
      argz.add(source);
    }

    return myFactory.executeCommandLine(
            context,
            nuget.getNuGetExeFile(),
            argz,
            workingDirectory
    );
  }
}
