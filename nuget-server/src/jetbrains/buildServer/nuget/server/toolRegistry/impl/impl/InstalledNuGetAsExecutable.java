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
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Evgeniy.Koshkin
 */
public class InstalledNuGetAsExecutable implements InstalledTool {
  private static final Logger LOG = Logger.getInstance(InstalledNuGetAsExecutable.class.getName());

  @NotNull private final PluginNaming myNaming;
  @NotNull private final ToolPacker myPacker;
  @NotNull private final File myPath;
  @NotNull private final File myUnpackFolder;

  protected InstalledNuGetAsExecutable(@NotNull PluginNaming naming, @NotNull ToolPacker packer, @NotNull File path) {
    myNaming = naming;
    myPacker = packer;
    myPath = path;
    myUnpackFolder = FileUtil.getCanonicalFile(myNaming.getUnpackedFolder(myPath));
  }

  @NotNull
  public File getAgentPluginFile() {
    return FileUtil.getCanonicalFile(myNaming.getAgentFile(myPath));
  }

  @NotNull
  public File getNuGetExePath() {
    return myPath;
  }

  public boolean isDefaultTool() {
    return false;
  }

  @NotNull
  public String getId() {
    return myPath.getName();
  }

  @NotNull
  public static String getVersionFromFileName(@NotNull String fileName){
    if (fileName.toLowerCase().endsWith(FeedConstants.EXE_EXTENSION.toLowerCase())) {
      fileName = fileName.substring(0, fileName.length() - FeedConstants.EXE_EXTENSION.length());
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

  public void install() {
    final File agentPluginFile = getAgentPluginFile();
    try {
      FileUtil.delete(agentPluginFile);
      FileUtil.delete(myUnpackFolder);

      FileUtil.createDir(myUnpackFolder);
      FileUtil.copy(myPath, new File(myUnpackFolder, PackagesConstants.NUGET_TOOL_REL_PATH));

      FileUtil.createParentDirs(agentPluginFile);
      myPacker.packTool(agentPluginFile, myUnpackFolder);
    } catch (Throwable t) {
      LOG.warn("Failed to install nuget tool: " + this);
      FileUtil.delete(agentPluginFile);
      FileUtil.delete(myUnpackFolder);
    }
  }

  public void delete() {
    FileUtil.delete(myPath);
    FileUtil.delete(getAgentPluginFile());
    FileUtil.delete(myUnpackFolder);
  }

  @Override
  public String toString() {
    return "InstalledNuGetAsExecutable{version=" + getVersion() +
            ", myPath=" + myPath +
            '}';
  }
}
