/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.07.11 14:27
 */
public interface NuGetFetchParameters extends NuGetParameters {

  /**
   * @return collection of nuget sources to fetch packages
   */
  @NotNull
  Collection<String> getNuGetPackageSources();

  /**
   * @return path to solution file.
   * @throws jetbrains.buildServer.RunBuildException
   *          if .sln file is not found
   */
  @NotNull
  File getSolutionFile() throws RunBuildException;
}
