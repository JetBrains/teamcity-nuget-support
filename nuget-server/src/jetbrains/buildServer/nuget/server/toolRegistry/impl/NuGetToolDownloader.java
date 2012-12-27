package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import org.jetbrains.annotations.NotNull;

/**
 * Created 27.12.12 18:51
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface NuGetToolDownloader {
  void installNuGet(@NotNull String packageId) throws ToolException;
}
