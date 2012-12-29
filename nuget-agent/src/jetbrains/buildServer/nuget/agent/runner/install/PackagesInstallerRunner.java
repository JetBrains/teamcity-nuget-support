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

package jetbrains.buildServer.nuget.agent.runner.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.NuGetRunnerBase;
import jetbrains.buildServer.nuget.agent.runner.install.impl.InstallStagesImpl;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 13:55
 */
public class PackagesInstallerRunner extends NuGetRunnerBase {
  private final InstallRunnerStagesBuilder myFactory;

  public PackagesInstallerRunner(@NotNull final NuGetActionFactory actionFactory,
                                 @NotNull final PackagesParametersFactory parametersFactory,
                                 @NotNull final InstallRunnerStagesBuilder factory) {
    super(actionFactory, parametersFactory);
    myFactory = factory;
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    CompositeBuildProcessImpl process = new CompositeBuildProcessImpl();
    final InstallStages stages = new InstallStagesImpl(process);
    createStages(context, stages);
    return process;
  }

  private void createStages(@NotNull final BuildRunnerContext context,
                            @NotNull final InstallStages stages) throws RunBuildException {
    final NuGetFetchParameters parameters = myParametersFactory.loadNuGetFetchParameters(context);
    final PackagesInstallParameters installParameters = myParametersFactory.loadInstallPackagesParameters(context, parameters);
    final PackagesUpdateParameters updateParameters = myParametersFactory.loadUpdatePackagesParameters(context, parameters);

    myFactory.buildStages(stages, context, parameters, installParameters, updateParameters);
  }

  @NotNull
  public String getType() {
    return PackagesConstants.INSTALL_RUN_TYPE;
  }
}
