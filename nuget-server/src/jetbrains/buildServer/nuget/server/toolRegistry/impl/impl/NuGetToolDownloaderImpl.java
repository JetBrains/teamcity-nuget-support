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

package jetbrains.buildServer.nuget.server.toolRegistry.impl.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.AvailableToolsState;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.NuGetToolDownloader;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.NuGetToolsInstaller;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static jetbrains.buildServer.nuget.common.FeedConstants.NUGET_EXTENSION;

/**
 * Created 27.12.12 18:48
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetToolDownloaderImpl implements NuGetToolDownloader {
  private static final Logger LOG = Logger.getInstance(NuGetToolDownloaderImpl.class.getName());
  private final FeedClient myFeed;
  private final NuGetFeedReader myClient;
  private final AvailableToolsState myState;
  private final NuGetToolsInstaller myInstaller;

  public NuGetToolDownloaderImpl(@NotNull final FeedClient feed,
                                 @NotNull final NuGetFeedReader client,
                                 @NotNull final AvailableToolsState state,
                                 @NotNull final NuGetToolsInstaller installer) {
    myFeed = feed;
    myClient = client;
    myState = state;
    myInstaller = installer;
  }

  @NotNull
  public String installNuGet(@NotNull final String packageId) throws ToolException {
    LOG.info("Start installing package " + packageId);

    final FeedPackage tool = myState.findTool(packageId);
    if (tool == null) {
      throw new ToolException("Failed to find package " + packageId);
    }

    LOG.info("Downloading package from: " + tool.getDownloadUrl());
    final String key = tool.getInfo().getId() + "." + tool.getInfo().getVersion();
    final File tmp = createTempFile(key);
    downloadPackage(tool, tmp);
    return myInstaller.installNuGet(key + NUGET_EXTENSION, tmp);
  }

  @NotNull
  private File createTempFile(@NotNull final String name) throws ToolException {
    try {
      File tempFile = FileUtil.createTempFile(name, NUGET_EXTENSION);
      FileUtil.createParentDirs(tempFile);
      return tempFile;
    } catch (IOException e) {
      String msg = "Failed to create temp file";
      LOG.debug(e);
      throw new ToolException(msg);
    }
  }

  private void downloadPackage(@NotNull final FeedPackage tool,
                               @NotNull final File file) throws ToolException {
    FileUtil.delete(file);
    try {
      myClient.downloadPackage(myFeed, tool, file);
    } catch (Exception e) {
      final PackageInfo info = tool.getInfo();

      LOG.warn("Failed to download package " + tool + " to " + file + ". " + e.getMessage());
      LOG.debug("Failed to download package " + tool + " to " + file + ". " + e.getMessage(), e);
      throw new ToolException("Failed to download package " + info.getId() + " " + info.getVersion() + ". " + e.getMessage());
    }
  }
}
