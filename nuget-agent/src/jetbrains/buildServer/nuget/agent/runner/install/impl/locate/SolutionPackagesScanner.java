/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.util.sln.SolutionFileParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 14:48
 */
public class SolutionPackagesScanner implements PackagesConfigScanner {
  private final SolutionFileParser myParser;

  public SolutionPackagesScanner(@NotNull final SolutionFileParser parser) {
    myParser = parser;
  }

  @NotNull
  public Collection<File> scanResourceConfig(@NotNull final BuildProgressLogger logger,
                                             @NotNull final File sln,
                                             @NotNull final File packages) throws RunBuildException {
    final Collection<File> result = new ArrayList<File>();
    final Collection<File> files = myParser.parseProjectFiles(logger, sln);
    for (File file : files) {
      final File config;
      if (file.isFile()) {
        File parentFile = file.getParentFile();
        if (parentFile == null){
          config = null;
        } else {
          config = new File(parentFile, "packages.config");
        }
      } else {
        config = new File(file, "packages.config");
      }

      if (config != null && config.isFile()) {
        result.add(config);
      }
    }

    return result;
  }
}
