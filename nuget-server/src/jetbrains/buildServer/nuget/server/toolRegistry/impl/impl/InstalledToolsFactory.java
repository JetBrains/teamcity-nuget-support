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
import jetbrains.buildServer.nuget.server.toolRegistry.impl.InstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.PluginNaming;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolPacker;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolUnpacker;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;

/**
 * @author Evgeniy.Koshkin
 */
public class InstalledToolsFactory {

  private static final Logger LOG = Logger.getInstance(InstalledToolsFactory.class.getName());

  @NotNull private final PluginNaming myNaming;
  @NotNull private final ToolPacker myPacker;
  @NotNull private final ToolUnpacker myUnpacker;

  public InstalledToolsFactory(final @NotNull PluginNaming naming, final @NotNull ToolPacker packer, final @NotNull ToolUnpacker unpacker) {
    myNaming = naming;
    myPacker = packer;
    myUnpacker = unpacker;
  }

  @Nullable
  public InstalledTool createToolForPath(@NotNull final File path) {
    if(FeedConstants.EXE_FILE_FILTER.accept(path)) return new InstalledNuGetAsExecutable(myNaming, myPacker, path);
    else if(FeedConstants.PACKAGE_FILE_FILTER.accept(path)) return new InstalledNuGetAsPackage(myNaming, myPacker, myUnpacker, path);
    LOG.debug("Path " + path + " can't be use as NuGet tool. Valid NuGet tool is a file with one of file extensions: " + FeedConstants.NUGET_EXTENSION  + ", " + FeedConstants.EXE_EXTENSION);
    return null;
  }
}
