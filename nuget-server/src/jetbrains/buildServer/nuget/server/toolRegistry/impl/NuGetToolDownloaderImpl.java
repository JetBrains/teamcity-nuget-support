package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
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
  private final NuGetFeedReader myClient;
  private final AvailableToolsState myState;
  private final NuGetToolsInstaller myInstaller;

  public NuGetToolDownloaderImpl(@NotNull final NuGetFeedReader client,
                                 @NotNull final AvailableToolsState state,
                                 @NotNull final NuGetToolsInstaller installer) {
    myClient = client;
    myState = state;
    myInstaller = installer;
  }

  public void installNuGet(@NotNull final String packageId) throws ToolException {
    LOG.info("Start installing package " + packageId);

    final FeedPackage tool = myState.findTool(packageId);
    if (tool == null) {
      throw new ToolException("Failed to find package " + packageId);
    }

    LOG.info("Downloading package from: " + tool.getDownloadUrl());
    final String key = tool.getInfo().getId() + "." + tool.getInfo().getVersion();
    final File tmp = createTempFile(key);
    downloadPackage(tool, tmp);
    myInstaller.installNuGet(key + NUGET_EXTENSION, tmp);
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
      myClient.downloadPackage(tool, file);
    } catch (Exception e) {
      final PackageInfo info = tool.getInfo();

      LOG.debug("Failed to download package " + tool + " to " + file + ". " + e.getMessage(), e);
      throw new ToolException("Failed to download package " + info.getId() + " " + info.getVersion() + ". " + e.getMessage());
    }
  }
}
