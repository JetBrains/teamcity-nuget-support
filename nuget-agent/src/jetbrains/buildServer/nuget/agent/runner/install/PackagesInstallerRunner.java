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
import jetbrains.buildServer.nuget.agent.runner.install.impl.InstallStages;
import jetbrains.buildServer.nuget.agent.runner.install.impl.InstallStagesImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.PackagesInstallerBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolver;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 13:55
 */
public class PackagesInstallerRunner extends NuGetRunnerBase {
  @NotNull
  private final RepositoryPathResolver myResolver;

  public PackagesInstallerRunner(@NotNull final NuGetActionFactory actionFactory,
                                 @NotNull final PackagesParametersFactory parametersFactory,
                                 @NotNull final RepositoryPathResolver resolver) {
    super(actionFactory, parametersFactory);
    myResolver = resolver;
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    CompositeBuildProcessImpl process = new CompositeBuildProcessImpl();
    InstallStages stages = new InstallStagesImpl(process);
    createStages(context, stages);
    return process;
  }

  private void createStages(@NotNull final BuildRunnerContext context,
                            @NotNull final InstallStages stages) throws RunBuildException {
    final NuGetFetchParameters parameters = myParametersFactory.loadNuGetFetchParameters(context);
    final PackagesInstallParameters installParameters = myParametersFactory.loadInstallPackagesParameters(context, parameters);
    final PackagesUpdateParameters updateParameters = myParametersFactory.loadUpdatePackagesParameters(context, parameters);

    if (installParameters == null) {
      throw new RunBuildException("NuGet install packages must be enabled");
    }

    final LocateNuGetConfigBuildProcess locate = new LocateNuGetConfigBuildProcess(
            parameters,
            context.getBuild().getBuildLogger(),
            myResolver,
            new PackagesInstallerBuilder(
                    myActionFactory,
                    stages,
                    context,
                    installParameters,
                    updateParameters
            ));

    stages.getLocateStage().pushBuildProcess(locate);
  }

  @NotNull
  public String getType() {
    return PackagesConstants.INSTALL_RUN_TYPE;
  }
}
