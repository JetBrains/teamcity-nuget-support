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

package jetbrains.buildServer.nuget.agent.publish;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.nuget.common.DotNetConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 15:15
 */
public class PublishCommandlineBuildServiceFactory implements CommandLineBuildServiceFactory, AgentBuildRunnerInfo {
  private static final Logger LOG = Logger.getInstance(PublishCommandlineBuildServiceFactory.class.getName());
  @NotNull
  public CommandLineBuildService createService() {
    return new PublishService();
  }

  @NotNull
  public AgentBuildRunnerInfo getBuildRunnerInfo() {
    return this;
  }

  @NotNull
  public String getType() {
    return PackagesConstants.PUBLISH_RUN_TYPE;
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