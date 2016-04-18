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
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetServerToolProvider;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.InstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.NuGetToolsSettings;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.tools.*;
import jetbrains.buildServer.tools.available.AvailableToolsState;
import jetbrains.buildServer.tools.available.DownloadableToolVersion;
import jetbrains.buildServer.tools.available.FetchAvailableToolsResult;
import jetbrains.buildServer.tools.available.FetchToolsPolicy;
import jetbrains.buildServer.tools.installed.ToolsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 1:07
 */
public class NuGetToolManagerImpl implements NuGetToolManager {

  private static final Logger LOG = Logger.getInstance(NuGetToolManagerImpl.class.getName());

  private final AvailableToolsState myAvailableTools;
  private final ToolsRegistry myInstalledTools;
  private final DefaultToolVersions myDefaultToolVersions;
  private final NuGetToolsSettings mySettings;

  public NuGetToolManagerImpl(@NotNull final AvailableToolsState availableTools,
                              @NotNull final ToolsRegistry installedTools,
                              @NotNull final DefaultToolVersions defaultToolVersions,
                              @NotNull final NuGetToolsSettings settings) {
    myAvailableTools = availableTools;
    myInstalledTools = installedTools;
    myDefaultToolVersions = defaultToolVersions;
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
  public File getNuGetPath(@Nullable final String toolRef, @NotNull final SProject scope) {
    if (!ToolVersionReference.isToolReference(toolRef)) {
      return new File(toolRef);
    }
    ToolVersion toolVersion;
    if(ToolVersionReference.isDefaultVersionReference(toolRef)){
      toolVersion = myDefaultToolVersions.getDefaultVersion(NuGetServerToolProvider.NUGET_TOOL_TYPE, scope);
    } else {
      toolVersion = new SimpleToolVersion(NuGetServerToolProvider.NUGET_TOOL_TYPE, ToolVersionReference.getToolVersionOfType(NuGetServerToolProvider.NUGET_TOOL_TYPE.getType(), toolRef));
    }
    if(toolVersion == null) return null;
    final File unpackedContentLocation = myInstalledTools.getUnpackedContentLocation(toolVersion);
    if(unpackedContentLocation == null){
      LOG.debug(String.format("Failed to locate unpacked %s on server", toolVersion));
      return null;
    }
    return new File(unpackedContentLocation, "");
  }

  @Nullable
  @Override
  public String getNuGetVersion(@Nullable final String toolRef, @NotNull final SProject scope) {
    if (!ToolVersionReference.isToolReference(toolRef)) {
      return null;
    }
    ToolVersion toolVersion;
    if(ToolVersionReference.isDefaultVersionReference(toolRef)){
      toolVersion = myDefaultToolVersions.getDefaultVersion(NuGetServerToolProvider.NUGET_TOOL_TYPE, scope);
    } else {
      toolVersion = new SimpleToolVersion(NuGetServerToolProvider.NUGET_TOOL_TYPE, ToolVersionReference.getToolVersionOfType(NuGetServerToolProvider.NUGET_TOOL_TYPE.getType(), toolRef));
    }
    if(toolVersion == null) return null;
    return toolVersion.getVersion();
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

}
