

package jetbrains.buildServer.nuget.server.tool.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.tool.NuGetToolDownloader;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.tools.available.DownloadableToolVersion;
import jetbrains.buildServer.tools.utils.URLDownloader;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetToolDownloaderImpl implements NuGetToolDownloader {
  private static final Logger LOG = Logger.getInstance(NuGetToolDownloaderImpl.class.getName());

  public void downloadTool(@NotNull DownloadableToolVersion tool, @NotNull File location) throws ToolException {
    LOG.info("Start installing package " + tool.getDisplayName());
    LOG.info("Downloading package from: " + tool.getDownloadUrl());
    try {
      URLDownloader.download(tool.getDownloadUrl(), location);
    } catch (Throwable e) {
      throw new ToolException("Failed to download package " + tool + " to " + location + e.getMessage(), e);
    }
    LOG.debug("Successfully downloaded package " + tool + " to " + location);
  }
}
