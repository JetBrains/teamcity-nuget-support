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

package jetbrains.buildServer.nuget.agent.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 11.07.11 14:57
*/
public class PackagesInstallerBuilder implements LocateNuGetConfigBuildProcess.Callback {
  private final NuGetActionFactory myActionFactory;
  private final BuildProcessContinuation myInstall;
  private final BuildProcessContinuation myUpdate;
  private final BuildProcessContinuation myPostUpdate;
  private final BuildRunnerContext myContext;

  private final PackagesUpdateParameters myUpdateParameters;
  private final PackagesInstallParameters myInstallParameters;

  public PackagesInstallerBuilder(@NotNull final NuGetActionFactory actionFactory,
                                  @NotNull final BuildProcessContinuation install,
                                  @NotNull final BuildProcessContinuation update,
                                  @NotNull final BuildProcessContinuation postUpdate,
                                  @NotNull final BuildRunnerContext context,
                                  @NotNull final PackagesInstallParameters installParameters,
                                  @Nullable final PackagesUpdateParameters updateParameters) {
    myInstall = install;
    myUpdate = update;
    myPostUpdate = postUpdate;
    myContext = context;
    myUpdateParameters = updateParameters;
    myInstallParameters = installParameters;
    myActionFactory = actionFactory;
  }

  public final void onPackagesConfigFound(@NotNull final File config, @NotNull final File targetFolder) {
    myInstall.pushBuildProcess(createInstallAction(config, targetFolder));

    if (myUpdateParameters == null) return;
    myUpdate.pushBuildProcess(createUpdateAction(config, targetFolder));
    myPostUpdate.pushBuildProcess(createInstallAction(config, targetFolder));
  }

  private DelegatingBuildProcess createUpdateAction(final File config, final File targetFolder) {
    return new DelegatingBuildProcess(
            new DelegatingBuildProcess.Action() {
              private final BuildProgressLogger logger = myContext.getBuild().getBuildLogger();

              @NotNull
              public BuildProcess startImpl() throws RunBuildException {
                String pathToLog = FileUtil.getRelativePath(myContext.getBuild().getCheckoutDirectory(), config);
                if (pathToLog == null) pathToLog = config.getPath();
                logger.activityStarted("update", "Updating NuGet packages for " + pathToLog, "nuget");

                return myActionFactory.createUpdate(myContext,
                        myUpdateParameters,
                        config,
                        targetFolder)
                        ;
              }

              public void finishedImpl() {
                logger.activityFinished("update", "nuget");
              }
            }
    );
  }

  private DelegatingBuildProcess createInstallAction(final File config, final File targetFolder) {
    return new DelegatingBuildProcess(
            new DelegatingBuildProcess.Action() {
              private final BuildProgressLogger logger = myContext.getBuild().getBuildLogger();

              @NotNull
              public BuildProcess startImpl() throws RunBuildException {
                String pathToLog = FileUtil.getRelativePath(myContext.getBuild().getCheckoutDirectory(), config);
                if (pathToLog == null) pathToLog = config.getPath();
                logger.activityStarted("install", "Installing NuGet packages for " + pathToLog, "nuget");


                return myActionFactory.createInstall(myContext,
                        myInstallParameters,
                        config,
                        targetFolder)
                        ;
              }

              public void finishedImpl() {
                logger.activityFinished("install", "nuget");
              }
            }
    );
  }
}
