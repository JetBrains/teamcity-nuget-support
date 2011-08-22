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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.08.11 20:47
 */
public class NuGetToolsInstaller {
  private static final Logger LOG = Logger.getInstance(NuGetToolsInstaller.class.getName());

  private final ToolPaths myToolPaths;
  private final NuGetFeedReader myClient;
  private final AvailableToolsState myState;
  private final ToolsWatcher myWatcher;

  public NuGetToolsInstaller(@NotNull final ToolPaths toolPaths,
                             @NotNull final NuGetFeedReader client,
                             @NotNull final AvailableToolsState state,
                             @NotNull final ToolsWatcher watcher) {
    myToolPaths = toolPaths;
    myClient = client;
    myState = state;
    myWatcher = watcher;
  }

  public void installNuGet(@NotNull final String packageId) throws ToolException {
    LOG.info("Start installing package " + packageId);

    final FeedPackage tool = myState.findTool(packageId);
    if (tool == null) {
      throw new ToolException("Failed to find package " + packageId);
    }

    LOG.info("Downloading package from: " + tool.getDownloadUrl());
    File dest = new File(myToolPaths.getPackages(), tool.getInfo().getId() + "." + tool.getInfo().getVersion() + ".nupkg");
    try {
      File tmp = File.createTempFile(dest.getName(), ".nupkg");
      FileUtil.createParentDirs(tmp);
      myClient.downloadPackage(tool, tmp);
      if (!tmp.renameTo(dest)) {
        throw new IOException("Failed to plug downloaded package to " + dest);
      }
    } catch (Exception e) {
      throw new ToolException("Failed to download package " + tool + ". " + e.getMessage(), e);
    }
    myWatcher.checkNow();
  }
}
