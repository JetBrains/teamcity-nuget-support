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
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 0:25
 */
public class ToolsRegistry {
  private static final Logger LOG = Logger.getInstance(ToolsRegistry.class.getName());

  private final ToolPaths myPaths;
  private final PluginNaming myNaming;
  private final ToolsWatcher myWatcher;

  public ToolsRegistry(@NotNull final ToolPaths paths,
                       @NotNull final PluginNaming naming,
                       @NotNull final ToolsWatcher watcher) {
    myPaths = paths;
    myNaming = naming;
    myWatcher = watcher;
  }

  @NotNull
  public Collection<? extends NuGetInstalledTool> getTools() {
    return getToolsInternal();
  }

  @Nullable
  public File getNuGetPath(@NotNull String path) {
    if (StringUtil.isEmptyOrSpaces(path)) return null;
    for (NuGetInstalledTool tool : getToolsInternal()) {
      if (tool.getId().equals(path)) {
        return tool.getPath();
      }
    }
    return null;
  }

  private Collection<InstalledTool> getToolsInternal() {
    final File[] tools = myPaths.getNuGetToolsPackages().listFiles(IS_PACKAGE);
    if (tools == null) return Collections.emptyList();

    final List<InstalledTool> result = new ArrayList<InstalledTool>();
    for (final File path : tools) {
      final InstalledTool e = new InstalledTool(myNaming, path);
      if (!e.getPath().isFile()) {
        LOG.warn("NuGet.exe is not found at " + e);
        continue;
      }

      if (!e.getAgentPluginFile().isFile()) {
        LOG.warn("NuGet tool is not packed for agent. " + e);
        continue;
      }
      result.add(e);
    }

    Collections.sort(result, new Comparator<InstalledTool>() {
      private int parse(String s) {
        try {
          s = s.trim();
          return Integer.parseInt(s);
        } catch (Throwable t) {
          return -1;
        }
      }

      public int compare(InstalledTool o1, InstalledTool o2) {
        final String[] version1 = o1.getVersion().split("\\.");
        final String[] version2 = o2.getVersion().split("\\.");

        for(int j = 0, jmax = Math.min(version1.length, version2.length); j < jmax; j++) {
          int v1 = parse(version1[j]);
          int v2 = parse(version2[j]);
          if (v1 < v2) return -1;
          if (v1 > v2) return 1;

          int i;
          if (v1 == -1 && v2 == -1 && (i = version1[j].compareToIgnoreCase(version2[j]))!= 0) {
            return i;
          }
        }

        if (version1.length > version2.length) return -1;
        return 1;
      }
    });

    return result;
  }

  public void removeTool(@NotNull final String toolId) {
    for (InstalledTool tool : getToolsInternal()) {
      if (tool.getId().equals(toolId)) {
        LOG.info("Removing NuGet plugin: " + tool);
        tool.delete();
      }
    }
    myWatcher.checkNow();
  }

  private static class InstalledTool implements NuGetInstalledTool {
    private final PluginNaming myNaming;
    private final File myPath;

    public InstalledTool(PluginNaming naming, @NotNull final File path) {
      myNaming = naming;
      myPath = path;
    }

    @NotNull
    public File getPath() {
      return FileUtil.getCanonicalFile(new File(myNaming.getUnpackedFolder(myPath), PackagesConstants.NUGET_TOOL_REL_PATH));
    }

    @NotNull
    public File getAgentPluginFile() {
      return FileUtil.getCanonicalFile(myNaming.getAgentFile(myPath));
    }

    public void delete() {
      FileUtil.delete(myPath);
    }

    @NotNull
    public String getId() {
      return myPath.getName();
    }

    @NotNull
    public String getVersion() {
      return myNaming.getVersion(myPath);
    }

    @Override
    public String toString() {
      return "InstalledTool{version=" + getVersion() +
              ", myPath=" + myPath +
              '}';
    }
  }

  private final FileFilter IS_PACKAGE = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isFile() && pathname.getName().endsWith(".nupkg");
    }
  };

}
