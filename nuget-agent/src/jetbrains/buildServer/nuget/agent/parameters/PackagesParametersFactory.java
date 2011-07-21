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

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 18:08
 */
public interface PackagesParametersFactory {
  /**
   * Creates object-style implementation of parameters
   *
   * @param context current build step context
   * @return parameters
   * @throws RunBuildException if failed to create parameters
   */
  @NotNull
  NuGetFetchParameters loadNuGetParameters(@NotNull final BuildRunnerContext context) throws RunBuildException;

  @Nullable
  PackagesInstallParameters loadInstallPackagesParameters(@NotNull final BuildRunnerContext context,
                                                          @NotNull final NuGetFetchParameters nuget) throws RunBuildException;

  @Nullable
  PackagesUpdateParameters loadUpdatePackagesParameters(@NotNull final BuildRunnerContext context,
                                                        @NotNull final NuGetFetchParameters nuget) throws RunBuildException;
}
