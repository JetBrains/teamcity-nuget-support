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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.NuGetToolReferenceUtils;
import jetbrains.buildServer.nuget.server.toolRegistry.*;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.*;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.tools.ToolVersion;
import jetbrains.buildServer.tools.available.AvailableToolsState;
import jetbrains.buildServer.tools.available.DownloadableToolVersion;
import jetbrains.buildServer.tools.available.FetchAvailableToolsResult;
import jetbrains.buildServer.tools.available.FetchToolsPolicy;
import jetbrains.buildServer.tools.installed.ToolsRegistry;
import jetbrains.buildServer.util.CollectionsUtil;
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

  private static final Logger LOG = Logger.getInstance(NuGetToolManagerImpl.class.getName());

  private final AvailableToolsState myAvailableTools;
  private final ToolsRegistry myInstalledTools;
  private final NuGetToolsSettings mySettings;

  public NuGetToolManagerImpl(@NotNull final AvailableToolsState availableTools,
                              @NotNull final ToolsRegistry installedTools,
                              @NotNull final NuGetToolsSettings settings) {
    myAvailableTools = availableTools;
    myInstalledTools = installedTools;
    mySettings = settings;
  }

  @NotNull
  public Collection<? extends NuGetInstalledTool> getInstalledTools() {
//    final String defaultToolId = getDefaultToolId();
//    final Collection<? extends NuGetInstalledTool> tools = myInstalledTools.getTools(NuGetServerToolProvider.NUGET_TOOL_TYPE);
//    if (defaultToolId == null || StringUtil.isEmptyOrSpaces(defaultToolId)) {
//      return tools;
//    }
//
//    final List<NuGetInstalledTool> toolsCopy = new ArrayList<NuGetInstalledTool>();
//    for (NuGetInstalledTool tool : tools) {
//      if (tool.getId().equals(defaultToolId)) {
//        toolsCopy.add(new DefaultTool(tool));
//      } else {
//        toolsCopy.add(tool);
//      }
//    }
//    return toolsCopy;
    return Collections.emptyList();
  }

  @NotNull
  public FetchAvailableToolsResult getAvailableTools(@NotNull FetchToolsPolicy policy) {
    final Set<String> installed = new HashSet<String>();
    for (NuGetInstalledTool tool : getInstalledTools()) {
      installed.add(tool.getVersion());
    }

    final FetchAvailableToolsResult fetchAvailableToolsResult = myAvailableTools.getAvailable(policy);
    final Collection<DownloadableToolVersion> available = new ArrayList<DownloadableToolVersion>(fetchAvailableToolsResult.getFetchedTools());
    final Iterator<DownloadableToolVersion> it = available.iterator();
    while (it.hasNext()) {
      ToolVersion next = it.next();
      if (installed.contains(next.getVersion())) {
        it.remove();
      }
    }
    return FetchAvailableToolsResult.create(available, fetchAvailableToolsResult.getErrors());
  }

  @Nullable
  public DownloadableNuGetTool findAvailableToolById(String toolId) {
    return new DownloadableNuGetTool(myAvailableTools.findTool(toolId));
  }

  @NotNull
  public NuGetTool installTool(@NotNull String toolId, @NotNull String toolFileName, @NotNull File toolFile) throws ToolException {
//    myInstaller.installNuGet(toolFileName, toolFile);
//    myWatcher.checkNow();
    return getInstalledTool(toolId);
  }

  public void removeTool(@NotNull String toolId) {
    try {
      myInstalledTools.removeTool(null);
    } catch (ToolException e) {
      LOG.error(e);
    }
  }

  @Nullable
  public String getNuGetPath(@Nullable final String toolRef) {
    final InstalledTool tool = findToolByRef(toolRef);
    return tool == null ? toolRef : tool.getNuGetExePath().getPath();
  }

  @Nullable
  @Override
  public String getNuGetVersion(@Nullable String toolRef) {
    final InstalledTool tool;
    try {
      tool = findToolByRef(toolRef);
    } catch (RuntimeException ex){
      LOG.warn(ex);
      return null;
    }
    return tool == null ? null : tool.getVersion();
  }

  @Nullable
  public NuGetInstalledTool getDefaultTool() {
//    InstalledTool tool = myInstalledTools.findTool(getDefaultToolId());
//    if (tool == null) return null;
//    return new DefaultTool(tool);
    return null;
  }

  public void setDefaultTool(@NotNull final String toolId) {
    mySettings.setDefaultTool(toolId);
  }

  @Nullable
  public String getDefaultToolId() {
    return mySettings.getDefaultToolId();
  }

  @NotNull
  private InstalledTool getInstalledTool(@NotNull final String toolId) throws ToolException {
    //final InstalledTool tool = myInstalledTools.findTool(toolId);
//    if (tool == null) {
//      throw new ToolException("Failed to find installed tool by id " + toolId);
//    }
//    return tool;
    return null;
  }

  @Nullable
  private InstalledTool findToolByRef(@Nullable final String toolRef) {
    if (toolRef == null || StringUtil.isEmptyOrSpaces(toolRef)) return null;
    if (NuGetToolReferenceUtils.isDefaultToolReference(toolRef)) {
      final String defaultToolId = getDefaultToolId();
      if (defaultToolId == null || StringUtil.isEmptyOrSpaces(defaultToolId)) {
        throw new RuntimeException("Failed to find default " + NUGET_COMMANDLINE + ". Default NuGet version is not selected.");
      }
//      final InstalledTool tool = myInstalledTools.findTool(defaultToolId);
//      if (tool == null) {
//        final String ref = NuGetToolReferenceUtils.getToolReference(defaultToolId);
//        throw new RuntimeException("Failed to find default " + NUGET_COMMANDLINE  + ". Specified id " + ref + " was not found");
//      }
//      return tool;
      return null;
    }
    final String toolId = NuGetToolReferenceUtils.getReferredToolId(toolRef);
    if (toolId == null) return null;
//    final InstalledTool tool = myInstalledTools.findTool(toolId);
//    if (tool == null) throw new RuntimeException("Failed to find " + NUGET_COMMANDLINE + " by id " + toolId);
//    else return tool;
    return null;
  }
}
