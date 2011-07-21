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

package jetbrains.buildServer.nuget.agent.commands;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 16:07
 */
public interface CommandFactory {

  @NotNull
  <T> T createInstall(@NotNull PackagesInstallParameters params,
                      @NotNull File packagesConfig,
                      @NotNull File targetFolder,
                      @NotNull Callback<T> factory) throws RunBuildException;

  @NotNull
  <T> T createUpdate(@NotNull PackagesUpdateParameters params,
                     @NotNull File packagesConfig,
                     @NotNull File targetFolder,
                     @NotNull Callback<T> factory) throws RunBuildException;

  @NotNull
  <T> T createPush(@NotNull NuGetPublishParameters params,
                   @NotNull File packagePath,
                   @NotNull Callback<T> factory) throws RunBuildException;

  public interface Callback<T> {
    /**
     * Called with generated arguments to crate execurable instance or run command
     * @param program program to run
     * @param workingDir working firectory of program
     * @param argz arguments array
     * @return some result object depending of caller's desire
     * @throws RunBuildException if failed to create/execure command
     */
    @NotNull
    T createCommand(@NotNull File program,
                    @NotNull final File workingDir,
                    @NotNull Collection<String> argz) throws RunBuildException;
  }
}
