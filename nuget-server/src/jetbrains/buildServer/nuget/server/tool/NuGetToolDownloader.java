

package jetbrains.buildServer.nuget.server.tool;

import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.tools.available.DownloadableToolVersion;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Evgeniy.Koshkin
 */
public interface NuGetToolDownloader {
  void downloadTool(@NotNull DownloadableToolVersion tool, @NotNull File location) throws ToolException;
}
