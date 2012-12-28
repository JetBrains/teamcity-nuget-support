package jetbrains.buildServer.nuget.server.toolRegistry.impl;

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
  Collection<? extends InstalledTool> getTools();

  @Nullable
  File getNuGetPath(@Nullable String toolId);

  @Nullable
  InstalledTool findTool(@Nullable String toolId);

  void removeTool(@NotNull String toolId);
}
