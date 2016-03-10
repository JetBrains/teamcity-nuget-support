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
import jetbrains.buildServer.nuget.feedReader.NuGetFeedClient;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolDownloader;
import jetbrains.buildServer.tools.ToolException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetToolDownloaderImpl implements NuGetToolDownloader {
  private static final Logger LOG = Logger.getInstance(NuGetToolDownloaderImpl.class.getName());

  @NotNull private final NuGetFeedReader myClient;
  @NotNull private final NuGetFeedClient myFeed;

  public NuGetToolDownloaderImpl(@NotNull final NuGetFeedReader client, @NotNull final NuGetFeedClient feed) {
    myClient = client;
    myFeed = feed;
  }

  @NotNull
  public void downloadTool(@NotNull DownloadableNuGetTool tool, @NotNull File location) throws ToolException {
    LOG.info("Start installing package " + tool.getId());
    LOG.info("Downloading package from: " + tool.getDownloadUrl());
    try {
      myClient.downloadPackage(myFeed, tool.getDownloadUrl(), location);
    } catch (Exception e) {
      LOG.warnAndDebugDetails("Failed to download package " + tool + " to " + location, e);
      throw new ToolException("Failed to download package " + tool.getId() + " " + tool.getVersion() + ". " + e.getMessage());
    }
  }
}
