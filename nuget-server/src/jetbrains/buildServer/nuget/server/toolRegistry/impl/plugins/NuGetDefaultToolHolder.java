package jetbrains.buildServer.nuget.server.toolRegistry.impl.plugins;

import jetbrains.buildServer.nuget.common.NuGetTools;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.InstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.NuGetToolsSettings;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolsRegistry;
import jetbrains.buildServer.serverSide.impl.agent.AgentPluginsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Created 28.12.12 13:01
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetDefaultToolHolder implements AgentPluginsProvider{
  public static final String NAME = NuGetTools.TOOL_DEFAULT_NAME + ".zip";
  private final NuGetToolsSettings mySettings;
  private final ToolsRegistry myRegistry;

  public NuGetDefaultToolHolder(@NotNull NuGetToolsSettings settings,
                                @NotNull ToolsRegistry registry) {
    mySettings = settings;
    myRegistry = registry;
  }

  @Nullable
  public File getPluginFile(@NotNull String s) {
    if (!NAME.equals(s)) return null;
    return getAgentPluginFile();
  }

  @Nullable
  private File getAgentPluginFile() {
    InstalledTool tool = myRegistry.findTool(mySettings.getDefaultToolId());
    if (tool == null) return null;
    return tool.getAgentPluginFile();
  }

  @NotNull
  public Map<String, File> getPlugins() {
    final File tool = getAgentPluginFile();
    if (tool != null) return Collections.singletonMap(NAME, tool);
    return Collections.emptyMap();
  }
}
