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

import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.08.11 20:47
 */
public class ToolPathsImpl implements ToolPaths {
  private final File myPluginRoot;

  public ToolPathsImpl(@NotNull final ServerPaths paths) {
    myPluginRoot = new File(paths.getPluginDataDirectory(), "jetbrains.nuget");
  }

  private File relative(@NotNull final String path) {
    final File pkgs = new File(myPluginRoot, path);
    //noinspection ResultOfMethodCallIgnored
    pkgs.mkdirs();
    return FileUtil.getCanonicalFile(pkgs);
  }

  @NotNull
  public File getPackages() {
    return relative("nupkg");
  }

  @NotNull
  public File getTools() {
    return relative("tools");
  }

  public File getAgentPluginsPath() {
    return relative("agent");
  }
}
