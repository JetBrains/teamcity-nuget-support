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

package jetbrains.buildServer.nuget.agent.util.impl;

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner2.GenericCommandLineBuildProcess;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 15:13
 */
public class NuGetCommandBuildProcessFactory implements CommandlineBuildProcessFactory {
  private final ExtensionHolder myExtensionHolder;
  private final NuGetCommandLineProvider myProvider;

  public NuGetCommandBuildProcessFactory(@NotNull final ExtensionHolder extensionHolder,
                                         @NotNull final NuGetCommandLineProvider commandLineProvider) {
    myExtensionHolder = extensionHolder;
    myProvider = commandLineProvider;
  }

  @NotNull
  public BuildProcess executeCommandLine(@NotNull final BuildRunnerContext context,
                                         @NotNull final String executable,
                                         @NotNull final Collection<String> arguments,
                                         @NotNull final File workingDir,
                                         @NotNull final Map<String, String> env) {
    final ProgramCommandLine programCommandLine = myProvider.getProgramCommandLine(context, executable, arguments, env);
    final CommandLineBuildSession commandLineBuildSession = new CommandLineBuildSession(programCommandLine, context);
    return new GenericCommandLineBuildProcess(context, commandLineBuildSession, myExtensionHolder);
  }
}
