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

package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.server.ToolPaths;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 1:00
 */
public class PluginNaming {
  private final ToolPaths myPaths;


  public PluginNaming(@NotNull final ToolPaths paths) {
    myPaths = paths;
  }

  @NotNull
  public File getUnpackedFolder(@NotNull final File packageFile) {
    //here we could take a look into .nuspec to fetch version and name
    return new File(myPaths.getNuGetToolsPath(), packageFile.getName());
  }

  @NotNull
  public File getAgentFile(@NotNull final File packageFile) {
    //here we could take a look into .nuspec to fetch version and name
    return new File(myPaths.getNuGetToolsAgentPluginsPath(), getAgentFileName(packageFile.getName()));
  }

  @NotNull
  public String getAgentFileName(@NotNull final String packageName) {
    return packageName + ".zip";
  }
}
