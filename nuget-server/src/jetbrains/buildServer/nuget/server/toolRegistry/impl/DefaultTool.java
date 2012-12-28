package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created 27.12.12 18:19
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class DefaultTool implements NuGetInstalledTool {
  private final NuGetInstalledTool myTool;

  public DefaultTool(@NotNull NuGetInstalledTool tool) {
    myTool = tool;
  }

  @NotNull
  public File getPath() {
    return myTool.getPath();
  }

  public boolean isDefaultTool() {
    return true;
  }

  @NotNull
  public String getId() {
    return myTool.getId();
  }

  @NotNull
  public String getVersion() {
    return myTool.getVersion();
  }
}
