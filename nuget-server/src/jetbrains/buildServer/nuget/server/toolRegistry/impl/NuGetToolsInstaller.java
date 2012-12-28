package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created 27.12.12 18:46
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface NuGetToolsInstaller {
  @NotNull
  String installNuGet(@NotNull String toolName, @NotNull File toolFile) throws ToolException;

  void validatePackage(@NotNull File packageFile) throws ToolException;
}
