/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.toolRegistry;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.feed.server.NuGetFeedUsageStatisticsProvider;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.tools.ToolVersion;
import jetbrains.buildServer.tools.installed.ToolsRegistry;
import jetbrains.buildServer.tools.usage.ToolUsageCalculator;
import jetbrains.buildServer.tools.usage.ToolVersionUsage;
import jetbrains.buildServer.usageStatistics.impl.providers.BaseExtensionUsageStatisticsProvider;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

import static jetbrains.buildServer.nuget.server.toolRegistry.NuGetServerToolProvider.NUGET_TOOL_TYPE;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetToolUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {

  private static final Logger LOG = Logger.getInstance(NuGetToolUsageStatisticsProvider.class.getName());
  private static final PositionAware NUGET_VERSIONS_GROUP = new PositionAware() {
    @NotNull
    public String getOrderId() {
      return "nuget_versions";
    }

    @NotNull
    public PositionConstraint getConstraint() {
      return PositionConstraint.after(NuGetFeedUsageStatisticsProvider.GROUP_NAME);
    }
  };

  @NotNull private final ToolsRegistry myToolsRegistry;
  @NotNull private final ToolUsageCalculator myToolUsageCalculator;

  public NuGetToolUsageStatisticsProvider(@NotNull final SBuildServer server,
                                          @NotNull ToolsRegistry toolsRegistry,
                                          @NotNull ToolUsageCalculator toolUsageCalculator) {
    myToolsRegistry = toolsRegistry;
    myToolUsageCalculator = toolUsageCalculator;
    myGroupName = "NuGet Versions";
    setIdFormat("jb.nuget.version.[%s]");
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "Build configurations count (% of active configurations with NuGet in use)";
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return NUGET_VERSIONS_GROUP;
  }

  @Override
  protected void collectUsages(@NotNull UsagesCollectorCallback usagesCollectorCallback) {
    final Collection<? extends ToolVersion> nugetVersions = myToolsRegistry.getTools(NUGET_TOOL_TYPE);
    if(nugetVersions.isEmpty()){
      LOG.debug(String.format("There are no %s tools installed on the server.", NUGET_TOOL_TYPE.getDisplayName()));
      return;
    }
    for (ToolVersion nugetVersion : nugetVersions){
      for (ToolVersionUsage usage : myToolUsageCalculator.getUsages(nugetVersion)){
        final String version = usage.getToolVersion().getVersion();
        usagesCollectorCallback.addUsage(version, version);
      }
    }
  }

  @Override
  protected int getTotalUsagesCount(@NotNull Map<ExtensionType, Integer> extensionUsages) {
    int result = 0;
    for (ToolVersion nugetVersion : myToolsRegistry.getTools(NUGET_TOOL_TYPE)){
      result += myToolUsageCalculator.getUsages(nugetVersion).size();
    }
    return result;
  }
}
