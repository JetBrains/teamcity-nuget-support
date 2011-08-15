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
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.zip.ZipInputStream;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.08.11 20:47
 */
public class NuGetToolsInstaller {
  private static final Logger LOG = Logger.getInstance(NuGetToolsInstaller.class.getName());

  private final ToolPaths myToolPaths;
  private final NuGetFeedReader myClient;
  private final AvailableToolsState myState;
  private final ToolPacker myPacker;
  private final PluginNaming myNaming;

  public NuGetToolsInstaller(@NotNull final ToolPaths toolPaths,
                             @NotNull final NuGetFeedReader client,
                             @NotNull final AvailableToolsState state,
                             @NotNull final ToolPacker packer,
                             @NotNull final PluginNaming naming) {
    myToolPaths = toolPaths;
    myClient = client;
    myState = state;
    myPacker = packer;
    myNaming = naming;
  }

  public void installNuGet(@NotNull final String packageId, @NotNull final InstallLogger logger) {
    logger.started(packageId);

    FeedPackage tool = null;
    try {
      tool = myState.findTool(packageId);
      if (tool == null) {
        logger.packageNotFound(packageId);
        return;
      }

      final File pkg = downloadPackage(logger, tool);
      final File dest = extractPackage(logger, tool, pkg);
      File agentTool = packAgentPlugin(logger, tool, dest);
      registerAgentPlugins(logger, tool, agentTool);
    } catch (ProcessedException e) {
      //NOP;
    } catch (Exception e) {
      LOG.warn("Failed to install NuGet.Commandline package. " + e.getMessage(), e);
    } finally {
      logger.finished(packageId, tool);
    }
  }

  @NotNull
  private File registerAgentPlugins(InstallLogger logger, FeedPackage tool, File agentTool) {
    logger.agentToolPubslishStarted(tool, agentTool);

    try {
      final File dest = myNaming.getAgetToolFilePath(tool);
      if (!agentTool.renameTo(dest)) {
        FileUtil.copy(agentTool, dest);
        FileUtil.delete(agentTool);
      }
      return dest;
    } catch (Exception e) {
      logger.agentToolPublishFailed(tool, agentTool, e);
      throw new ProcessedException();
    } finally {
      logger.agentToolPuglishFinished(tool, agentTool);
    }
  }

  private String getAgentToolFileName(@NotNull String version) {
    return "nuget-commnadline-" + version;
  }

  private File packAgentPlugin(@NotNull final InstallLogger logger,
                               @NotNull final FeedPackage tool,
                               @NotNull final File dest) {
    logger.agentToolPackStarted(tool, dest);
    try {
      return myPacker.packTool(getAgentToolFileName(tool.getInfo().getVersion()), dest);
    } catch (Exception e) {
      logger.agentToolPackFailed(tool, dest, e);
      LOG.warn("Failed to pack agent tool " + tool);
      throw new ProcessedException();
    } finally {
      logger.agentToolPackFinished(tool);
    }
  }

  @NotNull
  private File extractPackage(@NotNull final InstallLogger logger,
                              @NotNull final FeedPackage tool,
                              @NotNull final File pkg) {
    logger.packageUnpackStarted(tool, pkg);
    File dest = null;
    try {
      dest = new File(myToolPaths.getTools(), tool.getInfo().getVersion());
      FileUtil.createDir(dest);
      final ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(pkg)));
      if (!ArchiveUtil.unpackZip(zip, dest)) {
        throw new IOException("Failed to unpack package " + tool.getInfo() + " to " + dest);
      }
      return dest;
    } catch (Exception e) {
      logger.packageUnpackFailed(tool, pkg, dest);
      LOG.warn("Failed to unpack nuget package " + tool + ". " + e.getMessage(), e);
      throw new ProcessedException();
    } finally {
      logger.packageUnpackFinished(tool, pkg, dest);
    }
  }

  @NotNull
  private File downloadPackage(@NotNull final InstallLogger logger,
                               @NotNull final FeedPackage tool) {
    logger.packageDownloadStarted(tool);
    File pkg = null;
    try {
      pkg = FileUtil.createTempFile("nuget.commandline", ".nupkg");
      myClient.downloadPackage(tool, pkg);
      return pkg;
    } catch (Exception e) {
      LOG.warn("Failed to download package " + tool + (pkg != null ? " to file " + pkg : ""));
      logger.packageDownloadFailed(tool, pkg, e);
      throw new ProcessedException();
    } finally {
      logger.packageDownloadFinished(tool, pkg);
    }
  }

  private static class ProcessedException extends RuntimeException {
  }
}
