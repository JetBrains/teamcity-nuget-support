/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import jetbrains.buildServer.nuget.common.ToolIdUtils;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.AvailableToolsState;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.InstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.NuGetToolsInstaller;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolsRegistry;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.DownloadableNuGetTool;
import jetbrains.buildServer.tools.*;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Evgeniy.Koshkin on 15-Jan-16.
 */
public class NuGetToolProvider extends ToolProviderAdapter {
  private static final ToolTypeExtension NUGET_TOOL_TYPE = new ToolTypeExtension() {
    @NotNull
    public String getType() {
      return ToolConstants.NUGET_TOOL_TYPE_ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
      return "NuGet.exe";
    }

    @Nullable
    @Override
    public String getDescription() {
      return "Installed NuGet versions are automatically distributed to all build agents and can be used in NuGet-related runners.";
    }

    @Nullable
    @Override
    public String getFileName() {
      return "NuGet.exe";
    }

    @Override
    public boolean isSupportDownload() {
      return true;
    }
  };

  @NotNull private final ToolsRegistry myToolsRegistry;
  @NotNull private final NuGetToolsInstaller myToolInstaller;
  @NotNull private final AvailableToolsState myAvailableTools;
  @NotNull private final NuGetToolDownloader myToolDownloader;

  public NuGetToolProvider(@NotNull ToolsRegistry toolsRegistry,
                           @NotNull NuGetToolsInstaller toolInstaller,
                           @NotNull AvailableToolsState availableTools,
                           @NotNull NuGetToolDownloader toolDownloader) {
    myToolsRegistry = toolsRegistry;
    myToolInstaller = toolInstaller;
    myAvailableTools = availableTools;
    myToolDownloader = toolDownloader;
  }

  @NotNull
  public ToolType getType() {
    return NUGET_TOOL_TYPE;
  }

  @NotNull
  public Collection<ToolVersion> getInstalledToolVersions() {
    return CollectionsUtil.convertCollection(myToolsRegistry.getTools(), source -> new ToolVersion(NUGET_TOOL_TYPE, source.getVersion()));
  }

  @NotNull
  @Override
  public Collection<ToolVersion> getAvailableToolVersions() {
    return CollectionsUtil.convertCollection(myAvailableTools.getAvailable(ToolsPolicy.FetchNew).getFetchedTools(), new Converter<ToolVersion, DownloadableNuGetTool>() {
      @Override
      public ToolVersion createFrom(@NotNull DownloadableNuGetTool source) {
        return new ToolVersion(NUGET_TOOL_TYPE, source.getVersion());
      }
    });
  }

  @NotNull
  @Override
  public void installTool(@NotNull ToolVersion toolVersion) throws ToolException {
    final DownloadableNuGetTool downloadableNuGetTool = CollectionsUtil.findFirst(myAvailableTools.getAvailable(ToolsPolicy.ReturnCached).getFetchedTools(), data -> data.getVersion().equalsIgnoreCase(toolVersion.getVersion()));
    if(downloadableNuGetTool == null){
      throw new ToolException("Failed to fetch tool " + toolVersion + ". Download source info wasn't prefetched.");
    }

    File uploadTarget;
    try {
      uploadTarget = FileUtil.createTempFile(toolVersion.toString(), ".tmp");
      FileUtil.createParentDirs(uploadTarget);
    } catch (IOException e) {
      throw new ToolException("Failed to create temp file", e);
    }
    FileUtil.delete(uploadTarget);
    try{
      myToolDownloader.downloadTool(downloadableNuGetTool, uploadTarget);
      myToolInstaller.installNuGet(downloadableNuGetTool.getDestinationFileName(), uploadTarget);
    } finally {
      FileUtil.delete(uploadTarget);
    }
  }

  @NotNull
  @Override
  public ToolVersion installTool(@NotNull File toolContent) throws ToolException {
    final String toolContentFileName = toolContent.getName();
    final String nugetVersion = ToolIdUtils.getVersionFromId(FilenameUtils.removeExtension(toolContentFileName));
    if(StringUtil.isEmpty(nugetVersion)) {
      throw new ToolException("Failed to determine NuGet version based on content of file " + toolContent.getAbsolutePath());
    }
    myToolInstaller.installNuGet(toolContentFileName, toolContent);
    return new ToolVersion(NUGET_TOOL_TYPE, nugetVersion);
  }

  @Override
  public void removeTool(@Nullable String toolVersion) {
    //TODO: simplify, skip ID processing, version is enough
    for(InstalledTool installedTool : myToolsRegistry.getTools()){
      if(installedTool.getVersion().equalsIgnoreCase(toolVersion)){
        myToolsRegistry.removeTool(installedTool.getId());
      }
    }
  }
}
