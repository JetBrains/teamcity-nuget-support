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

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import jetbrains.buildServer.RunBuildException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.EventListener;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.06.12 11:45
*/
public interface PackagesInstallerCallback extends EventListener {
  /**
   * Called when packages.config is found
   * @param config full path to packages.config file
   * @param repositoryPath target folder to store packages
   * @throws jetbrains.buildServer.RunBuildException on erorr
   */
  void onPackagesConfigFound(@NotNull File config, @NotNull File repositoryPath) throws RunBuildException;

  /**
   * Called when no packages.config are found
   * @throws jetbrains.buildServer.RunBuildException on erorr
   */
  void onNoPackagesConfigsFound() throws RunBuildException;

  /**
   * Called when solution file is found
   * @param sln path to sln file
   * @param repositoryPath target folder to store packages
   * @throws jetbrains.buildServer.RunBuildException on error
   */
  void onSolutionFileFound(@NotNull File sln, @NotNull File repositoryPath) throws RunBuildException;
}
