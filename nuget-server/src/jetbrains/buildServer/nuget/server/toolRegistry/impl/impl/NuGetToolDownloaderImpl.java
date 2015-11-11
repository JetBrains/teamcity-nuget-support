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
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolDownloader;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static jetbrains.buildServer.nuget.common.FeedConstants.NUGET_EXTENSION;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetToolDownloaderImpl implements NuGetToolDownloader {
  private static final Logger LOG = Logger.getInstance(NuGetToolDownloaderImpl.class.getName());

  @NotNull private final NuGetFeedReader myClient;
  @NotNull private final FeedClient myFeed;

  public NuGetToolDownloaderImpl(@NotNull final NuGetFeedReader client, @NotNull final FeedClient feed) {
    myClient = client;
    myFeed = feed;
  }

  @NotNull
  public File downloadTool(@NotNull DownloadableNuGetTool tool) throws ToolException {
    LOG.info("Start installing package " + tool.getId());
    LOG.info("Downloading package from: " + tool.getDownloadUrl());
    File tempFile;
    try {
      tempFile = FileUtil.createTempFile(tool.getId(), NUGET_EXTENSION);
      FileUtil.createParentDirs(tempFile);
    } catch (IOException e) {
      String msg = "Failed to create temp file";
      LOG.debug(e);
      throw new ToolException(msg);
    }
    FileUtil.delete(tempFile);

    try {
      myClient.downloadPackage(myFeed, tool.getDownloadUrl(), tempFile);
    } catch (Exception e) {
      LOG.warn("Failed to download package " + tool + " to " + tempFile + ". " + e.getMessage());
      LOG.debug("Failed to download package " + tool + " to " + tempFile + ". " + e.getMessage(), e);
      throw new ToolException("Failed to download package " + tool.getId() + " " + tool.getVersion() + ". " + e.getMessage());
    }

    return tempFile;
  }
}
