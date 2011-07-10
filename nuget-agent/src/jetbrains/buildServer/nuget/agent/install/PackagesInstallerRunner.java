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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.nuget.agent.parameters.NuGetParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.DotNetConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static jetbrains.buildServer.nuget.agent.install.LocateNuGetConfigBuildProcess.Callback;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 13:55
 */
public class PackagesInstallerRunner implements AgentBuildRunner, AgentBuildRunnerInfo {
  private static final Logger LOG = Logger.getInstance(PackagesInstallerRunner.class.getName());

  private final NuGetActionFactory myInstallActionFactory;
  private final PackagesParametersFactory myParametersFactory;

  public PackagesInstallerRunner(@NotNull final NuGetActionFactory installActionFactory,
                                 @NotNull final PackagesParametersFactory parametersFactory) {
    myInstallActionFactory = installActionFactory;
    myParametersFactory = parametersFactory;
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    final CompositeBuildProcessImpl process = new CompositeBuildProcessImpl();
    final NuGetParameters parameters = myParametersFactory.loadNuGetParameters(context);
    final PackagesInstallParameters installParameters = myParametersFactory.loadInstallPackagesParameters(context, parameters);

    if (installParameters == null) {
      throw new RunBuildException("NuGet install packages must be enabled");
    }

    final LocateNuGetConfigBuildProcess locate = new LocateNuGetConfigBuildProcess(
            parameters,
            context.getBuild().getBuildLogger(),
            createLocateCallback(context, process, installParameters)
    );
    process.pushBuildProcess(locate);

    return process;
  }

  @NotNull
  private Callback createLocateCallback(@NotNull final BuildRunnerContext context,
                                        @NotNull final CompositeBuildProcessImpl process,
                                        @NotNull final PackagesInstallParameters parameters) {
    return new Callback() {
      public void onPackagesConfigFound(@NotNull final File config,
                                        @NotNull final File targetFolder) {
        process.pushBuildProcess(
                new DelegatingBuildProcess(
                        new DelegatingBuildProcess.Action() {
                          private final BuildProgressLogger logger = context.getBuild().getBuildLogger();

                          @NotNull
                          public BuildProcess startImpl() throws RunBuildException {
                            String pathToLog = FileUtil.getRelativePath(context.getBuild().getCheckoutDirectory(), config);
                            if (pathToLog == null) pathToLog = config.getPath();
                            logger.activityStarted("install", "Installing NuGet packages for " + pathToLog, "nuget");

                            return myInstallActionFactory.createInstall(context,
                                    parameters,
                                    config,
                                    targetFolder)
                                    ;
                          }

                          public void finishedImpl() {
                            logger.activityFinished("install", "nuget");
                          }
                        }
                )
        );
      }
    };
  }

  @NotNull
  public AgentBuildRunnerInfo getRunnerInfo() {
    return this;
  }

  @NotNull
  public String getType() {
    return PackagesConstants.RUN_TYPE;
  }

  public boolean canRun(@NotNull BuildAgentConfiguration agentConfiguration) {
    if (!agentConfiguration.getSystemInfo().isWindows()) {
      LOG.warn("NuGet packages installer available only under windows");
      return false;
    }

    if (!agentConfiguration.getConfigurationParameters().containsKey(DotNetConstants.DOT_NET_FRAMEWORK_4_x86)) {
      LOG.warn("NuGet requires .NET Framework 4.0 x86 installed");
      return false;
    }

    return true;
  }
}
