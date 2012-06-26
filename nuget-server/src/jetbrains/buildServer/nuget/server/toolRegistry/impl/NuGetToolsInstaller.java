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
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_TOOL_REL_PATH;

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

  public void installNuGet(@NotNull final String toolName,
                           @NotNull final File toolFile) throws ToolException {
    LOG.info("Start installing package " + toolName + " from file: " + toolFile);

    if (!toolName.toLowerCase().endsWith(".nupkg".toLowerCase())) {
      throw new ToolException("NuGet package file must have extension .nupkg");
    }

    final File dest = new File(myToolPaths.getNuGetToolsPackages(), toolName);
    validatePackage(toolFile);
    publishDownloadedPackage(dest, toolFile);
  }

  public void installNuGet(@NotNull final String packageId) throws ToolException {
    LOG.info("Start installing package " + packageId);

    final FeedPackage tool = myState.findTool(packageId);
    if (tool == null) {
      throw new ToolException("Failed to find package " + packageId);
    }

    LOG.info("Downloading package from: " + tool.getDownloadUrl());

    final File dest = new File(myToolPaths.getNuGetToolsPackages(), tool.getInfo().getId() + "." + tool.getInfo().getVersion() + ".nupkg");
    final File tmp = createTempFile(dest.getName());

    downloadPackage(tool, tmp);
    validatePackage(tmp);
    publishDownloadedPackage(dest, tmp);
  }

  @NotNull
  private File createTempFile(@NotNull final String name) throws ToolException {
    try {
      File tempFile = File.createTempFile(name, ".nupkg");
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
      myClient.downloadPackage(tool, file);
    } catch (Exception e) {
      final PackageInfo info = tool.getInfo();

      LOG.debug("Failed to download package " + tool + " to " + file + ". " + e.getMessage(), e);
      throw new ToolException("Failed to download package " + info.getId() + " " + info.getVersion() + file + ". " + e.getMessage());
    }
  }

  private void publishDownloadedPackage(@NotNull final File dest, @NotNull final File tmp) throws ToolException {
    if (dest.isFile()) {
      throw new ToolException("Tool with such version already exists");
    }

    try {
      FileUtil.copy(tmp, dest);
      FileUtil.delete(tmp);
    } catch (IOException e) {
      LOG.debug("Failed to copy downloaded package from " + tmp + " to " + dest + ". " + e.getMessage(), e);
      throw new ToolException("Failed to copy downloaded package. " + e.getMessage());
    }

    myWatcher.checkNow();
  }

  public void validatePackage(@NotNull final File pkg) throws ToolException {
    ZipFile file = null;
    try {
      file = new ZipFile(pkg);
      if (file.getEntry(NUGET_TOOL_REL_PATH) == null) {
        throw new ToolException("NuGet package must contain " + NUGET_TOOL_REL_PATH + " file");
      }
    } catch (IOException e) {
      String msg = "Failed to read NuGet package file. " + e.getMessage();
      LOG.warn(msg, e);
      throw new ToolException(msg);
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (IOException e) {
          //NOP
        }
      }
    }
  }
}
