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

package jetbrains.buildServer.nuget.server.toolRegistry;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.08.11 20:22
 */
public interface NuGetToolManager {
  /**
   * @return list of installed nuget tools
   */
  @NotNull
  Collection<? extends NuGetInstalledTool> getInstalledTools();

  /**
   * @return list of tools that are installing now
   */
  @NotNull
  Collection<NuGetInstallingTool> getInstallingTool();

  /**
   * @param policy kind of packages to return
   * @return fetches the full list of available and supported nuget tools
   * @throws FetchException on fetch error if it was called
   */
  @NotNull
  Collection<? extends NuGetTool> getAvailableTools(@NotNull final ToolsPolicy policy) throws FetchException;

  /**
   * Downloads and installs nuget tools for both server and agent
   * @param toolId tool id for tool to install
   */
  void installTool(@NotNull String toolId);


  /**
   * Removes tool from server and build agents
   * @param toolId tool id from {@link #getInstalledTools()} method
   */
  void removeTool(@NotNull String toolId);

}
