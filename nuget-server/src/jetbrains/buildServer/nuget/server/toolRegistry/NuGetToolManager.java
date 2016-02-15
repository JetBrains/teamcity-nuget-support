/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.DownloadableNuGetTool;
import jetbrains.buildServer.tools.ToolException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
   * @param policy kind of packages to return
   * @return fetches the full list of available and supported nuget tools and errors reported
   */
  @NotNull
  FetchAvailableToolsResult getAvailableTools(@NotNull final ToolsPolicy policy);

  @Nullable
  DownloadableNuGetTool findAvailableToolById(String toolId);

  /**
   * Installs tool from a given .nupkg file
   * @param toolFile path to Tool file to be copied as the result of install
   * @throws ToolException in case file is wrong
   * @return installed NuGet tool
   */
  @NotNull
  NuGetTool installTool(@NotNull final String toolId, @NotNull final String toolFileName, @NotNull File toolFile) throws ToolException;

  /**
   * Removes tool from server and build agents
   * @param toolId tool id from {@link #getInstalledTools()} method
   * @throws ToolException on tool installation error
   */
  void removeTool(@NotNull String toolId) throws ToolException;

  /**
   * Resolves path to NuGet.exe tool
   * @param path path to tool
   * @return resolved path of given path
   */
  @Nullable
  String getNuGetPath(@Nullable String path);

  /**
   * Resolves NuGet.exe tool version
   * @param path path to tool
   * @return resolved version of given path
   */
  @Nullable
  String getNuGetVersion(@Nullable String path);

  /**
   * Sets tool as default NuGet tool
   * @param toolId tool
   * @since v0.9
   */
  void setDefaultTool(@NotNull final String toolId);

  /**
   * @return default tool if exists, null otherwise
   * @since v0.9
   */
  @Nullable
  NuGetInstalledTool getDefaultTool();

  /**
   * @return default tool id, without respect to if it was removed
   * @since v0.9
   */
  @Nullable
  String getDefaultToolId();
}
