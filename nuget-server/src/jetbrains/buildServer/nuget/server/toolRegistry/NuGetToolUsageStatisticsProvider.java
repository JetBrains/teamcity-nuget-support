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

package jetbrains.buildServer.nuget.server.toolRegistry;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerUsageStatisticsProvider;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.impl.providers.BaseExtensionUsageStatisticsProvider;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.filters.Filter;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
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
      return PositionConstraint.after(NuGetServerUsageStatisticsProvider.NUGET_SERVER_STAT_GROUP_NAME);
    }
  };

  @NotNull private final SBuildServer myServer;
  @NotNull private final NuGetToolManager myNuGetToolManager;

  public NuGetToolUsageStatisticsProvider(@NotNull final SBuildServer server, @NotNull NuGetToolManager nuGetToolManager) {
    myServer = server;
    myNuGetToolManager = nuGetToolManager;
    myGroupName = "NuGet Versions";
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
    final Collection<? extends NuGetInstalledTool> installedTools = myNuGetToolManager.getInstalledTools();
    if(installedTools.isEmpty()){
      LOG.debug("There are no nuget.exe tools installed on the server.");
      return;
    }
    for(SBuildRunnerDescriptor buildRunner : getNuGetEnabledBuildRunners()){
      final Map<String, String> buildParameters = buildRunner.getParameters();
      final String toolPath = myNuGetToolManager.getNuGetPath(buildParameters.get(PackagesConstants.NUGET_PATH));
      final NuGetInstalledTool referredTool = CollectionsUtil.findFirst(installedTools, new Filter<NuGetInstalledTool>() {
        public boolean accept(@NotNull NuGetInstalledTool data) {
          return data.getPath().getAbsolutePath().equalsIgnoreCase(toolPath);
        }
      });
      if(referredTool == null) continue;
      final String referredToolVersion = referredTool.getVersion();
      usagesCollectorCallback.addUsage(referredToolVersion, referredToolVersion);
    }
  }

  @Override
  protected int getTotalUsagesCount(@NotNull Map<ExtensionType, Integer> extensionUsages) {
    return getNuGetEnabledBuildRunners().size();
  }

  private Collection<SBuildRunnerDescriptor> getNuGetEnabledBuildRunners() {
    Collection<SBuildRunnerDescriptor> result = new HashSet<SBuildRunnerDescriptor>();
    for(SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes())
      for (SBuildRunnerDescriptor buildRunner : buildType.getResolvedSettings().getBuildRunners()) {
        Map<String,String> buildParameters = buildRunner.getParameters();
        if(buildParameters.containsKey(PackagesConstants.NUGET_PATH))
          result.add(buildRunner);
      }
    return result;
  }
}
