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
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetVersionHolder;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.runner.install.InstallStages;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.07.11 14:57
 */
public class PackagesInstallerBuilder extends PackagesInstallerAdapter {
  private final NuGetActionFactory myActionFactory;
  private final InstallStages myStages;
  private final BuildRunnerContext myContext;
  private final NuGetVersionHolder myVersionHolder;

  private final PackagesInstallParameters myInstallParameters;

  public PackagesInstallerBuilder(@NotNull final NuGetActionFactory actionFactory,
                                  @NotNull final InstallStages stages,
                                  @NotNull final BuildRunnerContext context,
                                  @NotNull final PackagesInstallParameters installParameters,
                                  @NotNull final NuGetVersionHolder versionHolder) {
    myStages = stages;
    myContext = context;
    myInstallParameters = installParameters;
    myActionFactory = actionFactory;
    myVersionHolder = versionHolder;
  }

  public void onPackagesConfigFound(@NotNull final File config, @NotNull final File targetFolder) throws RunBuildException {
    myStages.getInstallStage().pushBuildProcess(
            new DelegatingBuildProcess(new DelegatingBuildProcess.Action() {
              @NotNull
              @Override
              public BuildProcess startImpl() throws RunBuildException {
                return myActionFactory.createInstall(
                        myContext,
                        myInstallParameters,
                        myVersionHolder.getNuGetVerion().supportInstallNoCache(),
                        config,
                        targetFolder);
              }
            })
    );
  }
}
