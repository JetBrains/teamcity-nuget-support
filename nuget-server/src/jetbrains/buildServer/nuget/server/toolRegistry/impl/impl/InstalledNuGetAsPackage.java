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

package jetbrains.buildServer.nuget.server.toolRegistry.impl.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.InstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.PluginNaming;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolPacker;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolUnpacker;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 17.07.12 12:30
*/
public class InstalledNuGetAsPackage implements InstalledTool {
  private static final Logger LOG = Logger.getInstance(InstalledNuGetAsPackage.class.getName());

  private final PluginNaming myNaming;
  private final ToolPacker myPacker;
  private final ToolUnpacker myUnpacker;
  private final File myPath;
  private File myUnpackFolder;

  protected InstalledNuGetAsPackage(@NotNull PluginNaming naming, @NotNull ToolPacker packer, @NotNull ToolUnpacker unpacker, @NotNull final File path) {
    myNaming = naming;
    myPacker = packer;
    myUnpacker = unpacker;
    myPath = path;
    myUnpackFolder = FileUtil.getCanonicalFile(myNaming.getUnpackedFolder(myPath));
  }

  public boolean isDefaultTool() {
    return false;
  }

  @NotNull
  public File getNuGetExePath() {
    return new File(myUnpackFolder, PackagesConstants.NUGET_TOOL_REL_PATH);
  }

  @NotNull
  public File getAgentPluginFile() {
    return FileUtil.getCanonicalFile(myNaming.getAgentFile(myPath));
  }

  public void install() {
    try {
      removeUnpackedFiles();

      FileUtil.createParentDirs(getAgentPluginFile());
      FileUtil.createDir(myUnpackFolder);
      myUnpacker.extractPackage(myPath, myUnpackFolder);
      myPacker.packTool(getAgentPluginFile(), myUnpackFolder);
    } catch (Throwable t) {
      LOG.warn("Failed to unpack nuget commandline: " + this);
      removeUnpackedFiles();
    }
  }

  public void delete() {
    LOG.info("Removing NuGet package: " + myPath);
    FileUtil.delete(myPath);
    removeUnpackedFiles();
  }

  @NotNull
  public String getId() {
    return myPath.getName();
  }

  @NotNull
  public static String getVersionFromFileName(@NotNull String fileName){
    if (fileName.toLowerCase().endsWith(FeedConstants.NUGET_EXTENSION.toLowerCase())) {
      fileName = fileName.substring(0, fileName.length() - FeedConstants.NUGET_EXTENSION.length());
      if (fileName.toLowerCase().startsWith(FeedConstants.NUGET_COMMANDLINE.toLowerCase() + ".")) {
        fileName = fileName.substring(FeedConstants.NUGET_COMMANDLINE.length() + 1);
      }
    }
    return fileName;
  }

  @NotNull
  public String getVersion() {
    return getVersionFromFileName(myPath.getName());
  }

  @Override
  public String toString() {
    return "InstalledNuGetAsPackage{version=" + getVersion() +
            ", myPath=" + myPath +
            '}';
  }

  private void removeUnpackedFiles() {
    FileUtil.delete(getAgentPluginFile());
    FileUtil.delete(myUnpackFolder);
  }
}
