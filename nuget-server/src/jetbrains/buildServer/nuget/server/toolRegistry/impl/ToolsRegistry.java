/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 0:25
 */
public class ToolsRegistry {
  private static final Logger LOG = Logger.getInstance(ToolsRegistry.class.getName());
  private final ToolPaths myPaths;
  private final PluginNaming myNaming;

  public ToolsRegistry(@NotNull final ToolPaths paths,
                       @NotNull final PluginNaming naming) {
    myPaths = paths;
    myNaming = naming;
  }

  @NotNull
  public Collection<? extends NuGetInstalledTool> getTools() {
    return getToolsInternal();
  }

  private Collection<InstalledTool> getToolsInternal() {
    final File[] tools = myPaths.getTools().listFiles();
    if (tools == null) return Collections.emptyList();
    final Collection<InstalledTool> result = new ArrayList<InstalledTool>();
    for (final File path : tools) {
      final InstalledTool e = new InstalledTool(path);
      if (!e.getPath().isFile()) {
        LOG.warn("NuGet.exe is not found at " + e);
        continue;
      }

      if (myNaming.getAgentToolFilePath(e).isFile()) {
        LOG.warn("NuGet tool is not packed for agent. " + e);
        continue;
      }
      result.add(e);
    }
    return result;
  }

  public void removeTool(@NotNull final String toolId) {
    for (InstalledTool tool : getToolsInternal()) {
      if (tool.getId().equals(toolId)) {
        LOG.info("Removing NuGet plugin: " + tool);

        final File agentPlugin = myNaming.getAgentToolFilePath(tool);
        LOG.info("Removing NuGet plugin agent tool : " + agentPlugin);
        FileUtil.delete(agentPlugin);

        final File toolHome = tool.getRootPath();
        LOG.info("Removing NuGet files from: " + toolHome);
        FileUtil.delete(toolHome);
        return;
      }
    }
  }

  private static class InstalledTool implements NuGetInstalledTool {
    private final File myPath;

    public InstalledTool(@NotNull final File path) {
      myPath = path;
    }

    @NotNull
    public File getRootPath() {
      return myPath;
    }

    @NotNull
    public File getPath() {
      return new File(myPath, "tools/NuGet.exe");
    }

    @NotNull
    public String getId() {
      return myPath.getName();
    }

    @NotNull
    public String getVersion() {
      return myPath.getName();
    }

    @Override
    public String toString() {
      return "InstalledTool{version=" + getVersion() +
              ", myPath=" + myPath +
              '}';
    }
  }
}
