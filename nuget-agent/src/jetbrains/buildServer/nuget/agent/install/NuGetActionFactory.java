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

package jetbrains.buildServer.nuget.agent.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 18:07
 */
public interface NuGetActionFactory {
  @NotNull
  BuildProcess createInstall(@NotNull BuildRunnerContext context,
                             @NotNull PackagesInstallParameters params,
                             @NotNull File packagesConfig,
                             @NotNull File targetFolder) throws RunBuildException;

  @NotNull
  BuildProcess createUpdate(@NotNull BuildRunnerContext context,
                            @NotNull PackagesUpdateParameters params,
                            @NotNull File packagesConfig,
                            @NotNull File targetFolder) throws RunBuildException;

  @NotNull
  BuildProcess createUsageReport(@NotNull BuildRunnerContext context,
                                 @NotNull NuGetFetchParameters params,
                                 @NotNull File packagesConfig,
                                 @NotNull File targetFolder) throws RunBuildException;
}
