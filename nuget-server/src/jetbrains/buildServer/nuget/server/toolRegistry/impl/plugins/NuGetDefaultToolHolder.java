/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.server.toolRegistry.impl.plugins;

import jetbrains.buildServer.nuget.common.NuGetToolReferenceUtils;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.InstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.NuGetToolsSettings;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.PluginNaming;
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
  private final NuGetToolsSettings mySettings;
  private final ToolsRegistry myRegistry;
  private final PluginNaming myNaming;

  public NuGetDefaultToolHolder(@NotNull NuGetToolsSettings settings,
                                @NotNull ToolsRegistry registry,
                                @NotNull PluginNaming naming) {
    mySettings = settings;
    myRegistry = registry;
    myNaming = naming;
  }

  @NotNull
  private String getPluginName() {
    return myNaming.getAgentFileName(NuGetToolReferenceUtils.getDefaultToolId());
  }

  @Nullable
  public File getPluginFile(@NotNull String s) {
    if (!getPluginName().equals(s)) return null;
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
    if (tool != null) return Collections.singletonMap(getPluginName(), tool);
    return Collections.emptyMap();
  }
}
