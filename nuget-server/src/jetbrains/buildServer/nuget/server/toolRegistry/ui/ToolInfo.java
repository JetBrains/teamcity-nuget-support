/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.toolRegistry.ui;

import jetbrains.buildServer.nuget.common.NuGetTools;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 11:00
 */
public class ToolInfo {
  private final String myId;
  private final String myVersion;

  public ToolInfo(@NotNull final NuGetInstalledTool tool) {
    this(NuGetTools.getToolReference(tool.getId()), tool.getVersion());
  }

  public ToolInfo(@NotNull final String id,
                  @NotNull final String version) {
    myId = id;
    myVersion = version;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @NotNull
  public String getVersion() {
    return myVersion;
  }
}
