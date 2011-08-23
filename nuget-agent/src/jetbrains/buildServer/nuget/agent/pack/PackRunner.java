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

package jetbrains.buildServer.nuget.agent.pack;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.util.CompositeBuildProcess;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.DotNetConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 12:11
 */
public class PackRunner implements AgentBuildRunner, AgentBuildRunnerInfo {
  private static final Logger LOG = Logger.getInstance(PackRunner.class.getName());

  private final NuGetActionFactory myActionFactory;
  private final PackagesParametersFactory myParametersFactory;

  public PackRunner(@NotNull final NuGetActionFactory actionFactory,
                    @NotNull final PackagesParametersFactory parametersFactory) {
    myActionFactory = actionFactory;
    myParametersFactory = parametersFactory;
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    final CompositeBuildProcess process = new CompositeBuildProcessImpl();
    final NuGetPackParameters params = myParametersFactory.loadPackParameters(context);

    process.pushBuildProcess(myActionFactory.createPack(context, params));
    return process;
  }

  @NotNull
  public AgentBuildRunnerInfo getRunnerInfo() {
    return this;
  }

  @NotNull
  public String getType() {
    return PackagesConstants.PACK_RUN_TYPE;
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
