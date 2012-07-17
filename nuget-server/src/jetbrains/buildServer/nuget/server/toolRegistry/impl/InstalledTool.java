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

package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 17.07.12 12:30
*/
public class InstalledTool implements NuGetInstalledTool {
  private static final Logger LOG = Logger.getInstance(InstalledTool.class.getName());

  private final PluginNaming myNaming;
  private final File myPath;

  public InstalledTool(@NotNull PluginNaming naming, @NotNull final File path) {
    myNaming = naming;
    myPath = path;
  }

  @NotNull
  public File getPath() {
    return new File(getUnpackFolder(), PackagesConstants.NUGET_TOOL_REL_PATH);
  }

  @NotNull
  public File getPackageFile() {
    return myPath;
  }

  @NotNull
  public File getUnpackFolder() {
    return FileUtil.getCanonicalFile(myNaming.getUnpackedFolder(myPath));
  }

  @NotNull
  public File getAgentPluginFile() {
    return FileUtil.getCanonicalFile(myNaming.getAgentFile(myPath));
  }

  public void delete() {
    LOG.info("Removing NuGet package: " + getPackageFile());
    FileUtil.delete(myPath);
    removeUnpackedFiles();
  }

  public void removeUnpackedFiles() {
    FileUtil.delete(getAgentPluginFile());
    FileUtil.delete(getUnpackFolder());
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
