package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 2:00
 */
public interface ToolPaths {
  @NotNull
  File getTools();

  File getAgentPluginsPath();
}
