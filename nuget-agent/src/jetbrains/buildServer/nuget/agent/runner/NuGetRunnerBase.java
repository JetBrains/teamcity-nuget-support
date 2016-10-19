/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 18:32
 */
public abstract class NuGetRunnerBase implements AgentBuildRunner, AgentBuildRunnerInfo {
  private final Pattern ourRequirementsPattern = Pattern.compile("^(" + DotNetConstants.DOTNET4VERSION_PATTERN + "|" + DotNetConstants.MONO_VERSION_PATTERN + ")$");
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
    if(CollectionsUtil.contains(agentConfiguration.getConfigurationParameters().keySet(), new Filter<String>() {
      public boolean accept(@NotNull String data) {
        return ourRequirementsPattern.matcher(data).find();
      }
    })) return true;

    LOG.warn("NuGet requires .NET Framework (x86) 4.0 and higher or Mono 3.2 and higher to be installed.");
    return false;
  }
}
