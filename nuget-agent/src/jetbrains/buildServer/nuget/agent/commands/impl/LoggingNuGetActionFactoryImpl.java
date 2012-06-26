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

package jetbrains.buildServer.nuget.agent.commands.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 19:57
 */
public class LoggingNuGetActionFactoryImpl implements NuGetActionFactory {
  private NuGetActionFactory myActionFactory;

  public LoggingNuGetActionFactoryImpl(@NotNull final NuGetActionFactory actionFactory) {
    myActionFactory = actionFactory;
  }

  @NotNull
  public BuildProcess createUsageReport(@NotNull BuildRunnerContext context, @NotNull File packagesConfig, @NotNull File targetFolder) throws RunBuildException {
    return myActionFactory.createUsageReport(context, packagesConfig, targetFolder);
  }

  @NotNull
  public BuildProcess createCreatedPackagesReport(@NotNull BuildRunnerContext context, @NotNull Collection<File> packageFiles) throws RunBuildException {
    return myActionFactory.createCreatedPackagesReport(context, packageFiles);
  }

  @NotNull
  public BuildProcess createPublishedPackageReport(@NotNull BuildRunnerContext context, @NotNull NuGetPublishParameters params, @NotNull File packageFile) throws RunBuildException {
    return myActionFactory.createPublishedPackageReport(context, params, packageFile);
  }

  @NotNull
  public BuildProcess createInstall(@NotNull final BuildRunnerContext context,
                                    @NotNull final PackagesInstallParameters params,
                                    @NotNull final File config,
                                    @NotNull final File targetFolder) {
    return new DelegatingBuildProcess(
            new LoggingAction(context, config, "install") {
              @NotNull
              @Override
              protected BuildProcess delegateToActualAction() throws RunBuildException {
                return myActionFactory.createInstall(
                        context,
                        params,
                        config,
                        targetFolder);
              }

              @NotNull
              @Override
              protected String getBlockDescription(@NotNull String pathToLog) {
                return "Installing NuGet packages for " + pathToLog;
              }
            }
    );
  }

  @NotNull
  public BuildProcess createUpdate(@NotNull final BuildRunnerContext context,
                                   @NotNull final PackagesUpdateParameters params,
                                   @NotNull final File config,
                                   @NotNull final File targetFolder) {
    return new DelegatingBuildProcess(
            new LoggingAction(context, config, "update") {
              @NotNull
              @Override
              protected BuildProcess delegateToActualAction() throws RunBuildException {
                return myActionFactory.createUpdate(
                        context,
                        params,
                        config,
                        targetFolder);
              }

              @NotNull
              @Override
              protected String getBlockDescription(@NotNull String pathToLog) {
                return "Updating NuGet packages for " + pathToLog;
              }
            }
    );
  }

  @NotNull
  public BuildProcess createPush(@NotNull final BuildRunnerContext context,
                                 @NotNull final NuGetPublishParameters params,
                                 @NotNull final File packagePath) throws RunBuildException {
    return new DelegatingBuildProcess(
            new LoggingAction(context, packagePath, "push") {
      @NotNull
      @Override
      protected BuildProcess delegateToActualAction() throws RunBuildException {
        return myActionFactory.createPush(context, params, packagePath);
      }

      @NotNull
      @Override
      protected String getBlockDescription(@NotNull String pathToLog) {
        return "Publish package " + pathToLog;
      }
    });
  }

  @NotNull
  public BuildProcess createPack(@NotNull final BuildRunnerContext context,
                                 @NotNull final File specFile,
                                 @NotNull final NuGetPackParameters params) throws RunBuildException {
    return new DelegatingBuildProcess(
            new LoggingAction(context, specFile, "pack") {
              @NotNull
              @Override
              protected BuildProcess delegateToActualAction() throws RunBuildException {
                return myActionFactory.createPack(context, specFile, params);
              }

              @NotNull
              @Override
              protected String getBlockDescription(@NotNull String pathToLog) {
                return "Create NuGet package from " + pathToLog;
              }
            });
  }

  private abstract class LoggingAction implements DelegatingBuildProcess.Action {
    private final BuildRunnerContext myContext;
    private final File myFileToLog;
    private final String myBlockName;

    protected LoggingAction(@NotNull final BuildRunnerContext context,
                            @NotNull final File fileToLog,
                            @NotNull final String blockName) {
      myContext = context;
      myFileToLog = fileToLog;
      myBlockName = blockName;
    }

    @NotNull
    protected abstract BuildProcess delegateToActualAction() throws RunBuildException;
    @NotNull
    protected abstract String getBlockDescription(@NotNull String pathToLog);


    @NotNull
    public BuildProcess startImpl() throws RunBuildException {
      String pathToLog = FileUtil.getRelativePath(myContext.getBuild().getCheckoutDirectory(), myFileToLog);
      if (pathToLog == null) pathToLog = myFileToLog.getPath();

      getLogger().activityStarted(myBlockName, getBlockDescription(pathToLog), "nuget");

      return delegateToActualAction();
    }

    @NotNull
    private BuildProgressLogger getLogger() {
      return myContext.getBuild().getBuildLogger();
    }

    public void finishedImpl() {
      getLogger().activityFinished(myBlockName, "nuget");
    }
  }

}
