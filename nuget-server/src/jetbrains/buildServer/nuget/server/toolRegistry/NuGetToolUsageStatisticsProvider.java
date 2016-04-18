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
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.feed.server.NuGetFeedUsageStatisticsProvider;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.usageStatistics.impl.providers.BaseExtensionUsageStatisticsProvider;
import jetbrains.buildServer.util.MultiMap;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

  @NotNull private final SBuildServer myServer;
  @NotNull private final NuGetToolManager myToolManager;

  public NuGetToolUsageStatisticsProvider(@NotNull final SBuildServer server, @NotNull NuGetToolManager toolManager) {
    myServer = server;
    myToolManager = toolManager;
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
    if(myToolManager.getInstalledTools().isEmpty()){
      LOG.debug(String.format("There are no %s tools installed on the server.", NuGetServerToolProvider.NUGET_TOOL_TYPE.getDisplayName()));
      return;
    }
    for(Map.Entry<SProject, List<SBuildRunnerDescriptor>> entry : getNuGetEnabledBuildRunners().entrySet()){
      for(SBuildRunnerDescriptor buildRunner : entry.getValue()){
        final Map<String, String> buildParameters = buildRunner.getParameters();
        String referredToolVersion = myToolManager.getNuGetVersion(buildParameters.get(PackagesConstants.NUGET_PATH), entry.getKey());
        if(referredToolVersion != null) {
          usagesCollectorCallback.addUsage(referredToolVersion, referredToolVersion);
        }
      }
    }
  }

  @Override
  protected int getTotalUsagesCount(@NotNull Map<ExtensionType, Integer> extensionUsages) {
    int size = 0;
    for(Map.Entry<SProject, List<SBuildRunnerDescriptor>> entry : getNuGetEnabledBuildRunners().entrySet()){
      size += entry.getValue().size();
    }
    return size;
  }

  private MultiMap<SProject, SBuildRunnerDescriptor> getNuGetEnabledBuildRunners() {
    MultiMap<SProject, SBuildRunnerDescriptor> result = new MultiMap<SProject, SBuildRunnerDescriptor>();
    for(SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes())
      for (SBuildRunnerDescriptor buildRunner : buildType.getResolvedSettings().getBuildRunners()) {
        Map<String,String> buildParameters = buildRunner.getParameters();
        if(buildParameters.containsKey(PackagesConstants.NUGET_PATH))
          result.putValue(buildType.getProject(), buildRunner);
      }
    return result;
  }
}
