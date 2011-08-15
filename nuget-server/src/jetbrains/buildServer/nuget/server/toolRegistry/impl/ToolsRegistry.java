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

import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
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
  private final ToolPaths myPaths;

  public ToolsRegistry(@NotNull final ToolPaths paths) {
    myPaths = paths;
  }

  @NotNull
  public Collection<NuGetInstalledTool> getTools() {
    final File[] tools = myPaths.getTools().listFiles();
    if (tools == null) return Collections.emptyList();
    final Collection<NuGetInstalledTool> result = new ArrayList<NuGetInstalledTool>();
    for (final File path : tools) {
      result.add(new NuGetInstalledTool() {
        @NotNull
        public File getPath() {
          return new File(path, "tools/NuGet.exe");
        }

        @NotNull
        public String getId() {
          return path.getName();
        }

        @NotNull
        public String getVersion() {
          return path.getName();
        }
      });
    }
    return result;
  }
}
