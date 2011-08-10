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
  Collection<NuGetInstalledTool> getInstalledTools();

  /**
   * @return fetches the full list of available and supported nuget tools
   */
  @NotNull
  Collection<NuGetTool> getAvailableTools();

  /**
   * Downloads and installs nuget tools for both server and agent
   * @param tool tool to download
   * @param progress callback for showing messages
   */
  void installTool(@NotNull NuGetTool tool, @NotNull ActionProgress progress);

  /**
   * Registers user-provided NuGet tool from given NuGetInstalledTool i
   * @param tool tool description to install
   * @param progress action progress callback
   */
  void registerCustomTool(@NotNull NuGetUserTool tool, @NotNull ActionProgress progress);
}
