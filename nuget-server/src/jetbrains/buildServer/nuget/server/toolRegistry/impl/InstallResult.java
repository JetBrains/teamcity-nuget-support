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

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 15.08.11 21:32
*/
public class InstallResult {
  private final File myExtractedPackage;
  private final File myAgentPlugin;

  public InstallResult(@NotNull final File extractedPackage,
                       @NotNull final File agentPlugin) {
    myExtractedPackage = extractedPackage;
    myAgentPlugin = agentPlugin;
  }

  @NotNull
  public File getExtractedPackage() {
    return myExtractedPackage;
  }

  @NotNull
  public File getAgentPlugin() {
    return myAgentPlugin;
  }
}
