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

package jetbrains.buildServer.nuget.server.toolRegistry.impl.impl;

import jetbrains.buildServer.nuget.common.NuGetToolReferenceUtils;
import jetbrains.buildServer.nuget.server.toolRegistry.*;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.*;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static jetbrains.buildServer.nuget.common.FeedConstants.NUGET_COMMANDLINE;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 1:07
 */
public class NuGetToolManagerImpl implements NuGetToolManager {
  private final AvailableToolsState myAvailableTools;
  private final ToolsRegistry myInstalledTools;
  private final NuGetToolsInstaller myInstaller;
  private final NuGetToolsSettings mySettings;

  public NuGetToolManagerImpl(@NotNull final AvailableToolsState availableTools,
                              @NotNull final NuGetToolsInstaller installer,
                              @NotNull final ToolsRegistry installedTools,
                              @NotNull final NuGetToolsSettings settings) {
    myAvailableTools = availableTools;
    myInstaller = installer;
    myInstalledTools = installedTools;
    mySettings = settings;
  }

  @NotNull
  public Collection<? extends NuGetInstalledTool> getInstalledTools() {
    final String defaultToolId = getDefaultToolId();
    final Collection<? extends NuGetInstalledTool> tools = myInstalledTools.getTools();
    if (defaultToolId == null || StringUtil.isEmptyOrSpaces(defaultToolId)) {
      return tools;
    }

    final List<NuGetInstalledTool> toolsCopy = new ArrayList<NuGetInstalledTool>();
    for (NuGetInstalledTool tool : tools) {
      if (tool.getId().equals(defaultToolId)) {
        toolsCopy.add(new DefaultTool(tool));
      } else {
        toolsCopy.add(tool);
      }
    }
    return toolsCopy;
  }

  @NotNull
  public FetchAvailableToolsResult getAvailableTools(@NotNull ToolsPolicy policy) {
    final Set<String> installed = new HashSet<String>();
    for (NuGetInstalledTool tool : getInstalledTools()) {
      installed.add(tool.getVersion());
    }

    final FetchAvailableToolsResult fetchAvailableToolsResult = myAvailableTools.getAvailable(policy);
    final Collection<DownloadableNuGetTool> available = new ArrayList<DownloadableNuGetTool>(fetchAvailableToolsResult.getFetchedTools());
    final Iterator<DownloadableNuGetTool> it = available.iterator();
    while (it.hasNext()) {
      NuGetTool next = it.next();
      if (installed.contains(next.getVersion())) {
        it.remove();
      }
    }
    return FetchAvailableToolsResult.create(available, fetchAvailableToolsResult.getErrors());
  }

  @Nullable
  public DownloadableNuGetTool findAvailableToolById(String toolId) {
    return myAvailableTools.findTool(toolId);
  }

  @NotNull
  public NuGetTool installTool(@NotNull String toolId, @NotNull String toolFileName, @NotNull File toolFile) throws ToolException {
    myInstaller.installNuGet(toolFileName, toolFile);
    return findInstalledTool(toolId);
  }

  @NotNull
  private InstalledTool findInstalledTool(@NotNull final String toolId) throws ToolException {
    final InstalledTool tool = findTool(toolId);
    if (tool == null) {
      throw new ToolException("Failed to find installed tool " + toolId);
    }
    return tool;
  }

  @Nullable
  private InstalledTool findTool(@Nullable final String toolId) {
    return myInstalledTools.findTool(toolId);
  }

  public void removeTool(@NotNull String toolId) {
    myInstalledTools.removeTool(toolId);
  }

  @Nullable
  public String getNuGetPath(@Nullable final String path) {
    if (path == null || StringUtil.isEmptyOrSpaces(path)) return path;

    if (NuGetToolReferenceUtils.isDefaultToolReference(path)) {
      final String id = getDefaultToolId();
      if (id == null || StringUtil.isEmptyOrSpaces(id)) {
        throw new RuntimeException("Failed to find default " + NUGET_COMMANDLINE + ". Default NuGet version is not selected");
      }

      final String ref = NuGetToolReferenceUtils.getToolReference(id);
      final InstalledTool nuGetPath = findTool(id);
      if (nuGetPath == null) {
        throw new RuntimeException("Failed to find default " + NUGET_COMMANDLINE  + ". Specified version " + ref + " was not found");
      }
      return nuGetPath.getNuGetExePath().getPath();
    }

    final String id = NuGetToolReferenceUtils.getReferredToolId(path);
    if (id == null) return path;
    final InstalledTool nuGetPath = findTool(id);
    if (nuGetPath != null) {
      return nuGetPath.getNuGetExePath().getPath();
    }
    throw new RuntimeException("Failed to find " + NUGET_COMMANDLINE + " version " + id);
  }

  @Nullable
  public NuGetInstalledTool getDefaultTool() {
    InstalledTool tool = findTool(getDefaultToolId());
    if (tool == null) return null;
    return new DefaultTool(tool);
  }

  public void setDefaultTool(@NotNull final String toolId) {
    mySettings.setDefaultTool(toolId);
  }

  @Nullable
  public String getDefaultToolId() {
    return mySettings.getDefaultToolId();
  }
}
