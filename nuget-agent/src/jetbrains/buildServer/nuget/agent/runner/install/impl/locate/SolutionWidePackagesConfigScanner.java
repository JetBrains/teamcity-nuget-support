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
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 15:49
 */
public class SolutionWidePackagesConfigScanner implements PackagesConfigScanner {

  @NotNull
  public Collection<File> scanResourceConfig(@NotNull BuildProgressLogger logger, @NotNull File sln, @NotNull File packages) throws RunBuildException {
    final File solutionPackagesConfig = findSolutionPackagesConfigFile(sln);
    if (solutionPackagesConfig != null) {
      logger.message("Found solution-wide packages.config: " + solutionPackagesConfig);
      return Collections.singleton(solutionPackagesConfig);
    }

    return Collections.emptyList();
  }

  @Nullable
  private File findSolutionPackagesConfigFile(@NotNull final File sln) {
    final File parentFile = sln.getParentFile();
    if (parentFile == null) return null;

    final File path = new File(parentFile, ".nuget/packages.config");

    if (path.isFile()) return path;
    return null;
  }

}
