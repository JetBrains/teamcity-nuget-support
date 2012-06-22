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

package jetbrains.buildServer.nuget.agent.commands.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.CommandFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 11:59
 */
public class NuGetProcessCallbackImpl implements NuGetProcessCallback {
  private final CommandlineBuildProcessFactory myFactory;

  public NuGetProcessCallbackImpl(@NotNull final CommandlineBuildProcessFactory factory) {
    myFactory = factory;
  }

  @NotNull
  public CommandFactory.Callback<BuildProcess> getCallback(@NotNull final BuildRunnerContext context,
                                                           @NotNull final Collection<PackageSource> sources) {
    return new CommandFactory.Callback<BuildProcess>() {
      @NotNull
      public BuildProcess createCommand(@NotNull final File program,
                                        @NotNull final File workingDir,
                                        @NotNull final Collection<String> _argz,
                                        @NotNull final Map<String, String> additionalEnvironment) throws RunBuildException {
        if (!program.isFile()) {
          throw new RunBuildException("Failed to find NuGet executable at: " + program);
        }

        return myFactory.executeCommandLine(
                context,
                program.getPath(),
                _argz,
                workingDir,
                additionalEnvironment
        );
      }
    };
  }
}
