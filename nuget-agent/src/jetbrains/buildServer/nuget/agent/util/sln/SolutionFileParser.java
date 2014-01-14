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

package jetbrains.buildServer.nuget.agent.util.sln;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 20:04
 */
public interface SolutionFileParser {
  /**
   * Parses .sln file to fetch project files from it.
   * Web projects does not have explicit project files, thus,
   * web project home directory will be returned.
   *
   *
   * @param logger logger to log parse progress
   * @param sln path to solution file
   * @return collection of full paths to referenced projects files or home directories
   */
  @NotNull
  Collection<File> parseProjectFiles(@NotNull final BuildProgressLogger logger,
                                     @NotNull final File sln) throws RunBuildException;
}
