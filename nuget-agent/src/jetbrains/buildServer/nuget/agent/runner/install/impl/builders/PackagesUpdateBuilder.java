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

package jetbrains.buildServer.nuget.agent.runner.install.impl.builders;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetVersionHolder;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.install.InstallStages;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static jetbrains.buildServer.nuget.common.PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 12:10
 */
public class PackagesUpdateBuilder extends PackagesInstallerAdapter {
  private final NuGetActionFactory myActionFactory;
  private final InstallStages myStages;
  private final BuildRunnerContext myContext;
  private final NuGetVersionHolder myVersionHolder;
  private final PackagesInstallParameters myInstallParameters;
  private final PackagesUpdateParameters myUpdateParameters;

  public PackagesUpdateBuilder(@NotNull final NuGetActionFactory actionFactory,
                               @NotNull final InstallStages stages,
                               @NotNull final BuildRunnerContext context,
                               @NotNull final NuGetVersionHolder versionHolder,
                               @NotNull final PackagesInstallParameters installParameters,
                               @Nullable final PackagesUpdateParameters updateParameters) {
    myStages = stages;
    myContext = context;
    myVersionHolder = versionHolder;
    myInstallParameters = installParameters;
    myUpdateParameters = updateParameters;
    myActionFactory = actionFactory;
  }

  public void onSolutionFileFound(@NotNull File sln, @NotNull File targetFolder) throws RunBuildException {
    super.onSolutionFileFound(sln, targetFolder);

    if (myUpdateParameters.getUpdateMode() != PackagesUpdateMode.FOR_SLN) return;

    myStages.getUpdateStage().pushBuildProcess(
            myActionFactory.createUpdate(
                    myContext,
                    myUpdateParameters,
                    sln,
                    targetFolder
            )
    );
  }

  public void onPackagesConfigFound(@NotNull final File config, @NotNull final File targetFolder) throws RunBuildException {
    super.onPackagesConfigFound(config, targetFolder);

    if (myUpdateParameters.getUpdateMode() == FOR_EACH_PACKAGES_CONFIG) {
      myStages.getUpdateStage().pushBuildProcess(
              myActionFactory.createUpdate(
                      myContext,
                      myUpdateParameters,
                      config,
                      targetFolder
              )
      );
    }

    ///NOTE: tricks to workaround NuGet's cool feature
    ///NOTE: http://nuget.codeplex.com/workitem/2017
    myStages.getPostUpdateStart().pushBuildProcess(
            new DelegatingBuildProcess(new DelegatingBuildProcess.Action() {
              @NotNull
              @Override
              public BuildProcess startImpl() throws RunBuildException {
                if (config.isFile()) {
                  return myActionFactory.createInstall(
                          myContext,
                          myInstallParameters,
                          myVersionHolder.getNuGetVerion().supportInstallNoCache(),
                          config,
                          targetFolder);
                } else {
                  return new BuildProcessBase() {
                    @NotNull
                    @Override
                    protected BuildFinishedStatus waitForImpl() throws RunBuildException {
                      BuildProgressLogger log = myContext.getBuild().getBuildLogger();
                      log.warning("Packages.config file was removed by NuGet.exe update command. See http://nuget.codeplex.com/workitem/2017 for details.");
                      return BuildFinishedStatus.FINISHED_SUCCESS;
                    }
                  };
                }
              }
            }));
  }
}
