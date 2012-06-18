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

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 14:09
 */
public class LocateNuGetConfigProcessFactory {
  private final RepositoryPathResolver myResolver;
  private final List<PackagesConfigScanner> myScanners;

  public LocateNuGetConfigProcessFactory(@NotNull final RepositoryPathResolver resolver,
                                         @NotNull final List<PackagesConfigScanner> scanners) {
    myResolver = resolver;
    myScanners = scanners;
  }

  @NotNull
  public LocateNuGetConfigBuildProcess createPrecess(@NotNull final BuildRunnerContext context,
                                                     @NotNull final NuGetFetchParameters parameters) {
    return new LocateNuGetConfigBuildProcess(
            parameters,
            context.getBuild().getBuildLogger(),
            myResolver,
            myScanners);
  }

}
