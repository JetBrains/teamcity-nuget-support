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

package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.NuGetTools;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsWriter;
import jetbrains.buildServer.nuget.server.toolRegistry.*;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 1:07
 */
public class NuGetToolManagerImpl implements NuGetToolManager {
  private static final String DEFAULT_NUGET_KEY = "default-nuget";

  private final AvailableToolsState myAvailables;
  private final NuGetToolsInstaller myInstaller;
  private final NuGetToolDownloader myDownloader;
  private final ToolsRegistry myInstalled;
  private final NuGetSettingsManager mySettings;

  public NuGetToolManagerImpl(@NotNull final AvailableToolsState availables,
                              @NotNull final NuGetToolsInstaller installer,
                              @NotNull final NuGetToolDownloader downloader,
                              @NotNull final ToolsRegistry installed,
                              @NotNull final NuGetSettingsManager settings) {
    myAvailables = availables;
    myInstaller = installer;
    myDownloader = downloader;
    myInstalled = installed;
    mySettings = settings;
  }

  @NotNull
  public Collection<? extends NuGetInstalledTool> getInstalledTools() {
    final String defaultToolId = getDefaultToolId();
    final Collection<? extends NuGetInstalledTool> tools = myInstalled.getTools();
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
  public Collection<? extends NuGetTool> getAvailableTools(@NotNull ToolsPolicy policy) throws FetchException {
    final Set<String> installed = new HashSet<String>();
    for (NuGetInstalledTool tool : getInstalledTools()) {
      installed.add(tool.getVersion());
    }
    //This must be cached to make if work faster!
    final Collection<NuGetTool> available = new ArrayList<NuGetTool>(myAvailables.getAvailable(policy));
    final Iterator<NuGetTool> it = available.iterator();
    while (it.hasNext()) {
      NuGetTool next = it.next();
      if (installed.contains(next.getVersion())) {
        it.remove();
      }
    }
    return available;
  }

  public void installTool(@NotNull String toolId) throws ToolException {
    myDownloader.installNuGet(toolId);
  }

  public void installTool(@NotNull String toolName, @NotNull File toolFile) throws ToolException {
    myInstaller.installNuGet(toolName, toolFile);
  }

  public void removeTool(@NotNull String toolId) {
    myInstalled.removeTool(toolId);
  }

  @Nullable
  public String getNuGetPath(@Nullable final String path) {
    if (path == null || StringUtil.isEmptyOrSpaces(path)) return path;
    final String id = NuGetTools.getReferredToolId(path);
    if (id == null) return path;
    final File nuGetPath = myInstalled.getNuGetPath(id);
    if (nuGetPath != null) {
      return nuGetPath.getPath();
    }
    throw new RuntimeException("Failed to find " + FeedConstants.NUGET_COMMANDLINE + " version " + id);
  }

  @Nullable
  public NuGetInstalledTool getDefaultTool() {
    for (NuGetInstalledTool tool : getInstalledTools()) {
      if (tool.isDefault()) return tool;
    }
    return null;
  }

  public void setDefaultTool(@NotNull final String toolId) {
    mySettings.writeSettings(NuGetSettingsComponent.NUGET, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.setStringParameter(DEFAULT_NUGET_KEY, toolId);
        return null;
      }
    });
  }

  @Nullable
  public String getDefaultToolId() {
    return mySettings.readSettings(NuGetSettingsComponent.NUGET, new NuGetSettingsManager.Func<NuGetSettingsReader, String>() {
      public String executeAction(@NotNull NuGetSettingsReader action) {
        return action.getStringParameter(DEFAULT_NUGET_KEY);
      }
    });
  }
}
