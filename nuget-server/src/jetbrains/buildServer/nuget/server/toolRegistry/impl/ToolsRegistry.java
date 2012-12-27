package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * Created 27.12.12 19:02
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface ToolsRegistry {
  @NotNull
  Collection<? extends NuGetInstalledTool> getTools();

  @Nullable
  File getNuGetPath(@NotNull String path);

  void removeTool(@NotNull String toolId);
}
