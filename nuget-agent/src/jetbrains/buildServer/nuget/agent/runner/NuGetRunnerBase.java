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

package jetbrains.buildServer.nuget.agent.runner;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.common.DotNetConstants;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 18:32
 */
public abstract class NuGetRunnerBase implements AgentBuildRunner, AgentBuildRunnerInfo {
  protected final Logger LOG = Logger.getInstance(getClass().getName());

  protected final NuGetActionFactory myActionFactory;
  protected final PackagesParametersFactory myParametersFactory;

  public NuGetRunnerBase(NuGetActionFactory actionFactory, PackagesParametersFactory parametersFactory) {
    myActionFactory = actionFactory;
    myParametersFactory = parametersFactory;
  }

  @NotNull
  public AgentBuildRunnerInfo getRunnerInfo() {
    return this;
  }

  @NotNull
  public abstract String getType();

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
