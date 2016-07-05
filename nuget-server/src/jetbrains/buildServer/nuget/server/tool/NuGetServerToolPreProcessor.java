/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.tool;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.tools.ServerToolPreProcessor;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.tools.installed.ToolPaths;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Created by Evgeniy.Koshkin.
 */
public class NuGetServerToolPreProcessor implements ServerToolPreProcessor {
  private static final Logger LOG = Logger.getInstance(NuGetServerToolPreProcessor.class.getName());
  private static final String NUPKG = "nupkg";
  private static final String AGENT = "agent";
  private static final String TOOLS = "tools";

  private final ServerPaths myServerPaths;
  private final ToolPaths myToolPaths;

  public NuGetServerToolPreProcessor(@NotNull final ServerPaths serverPaths,
                                     @NotNull final ToolPaths toolPaths) {
    myServerPaths = serverPaths;
    myToolPaths = toolPaths;
  }

  @NotNull
  @Override
  public String getName() {
    return NuGetServerToolProvider.NUGET_TOOL_TYPE.getType();
  }

  @Override
  public void preProcess() throws ToolException {
    final File nugetPluginDataDir = new File(myServerPaths.getPluginDataDirectory(), FeedConstants.NUGET_COMMANDLINE);

    final File oldNuGetPackagesLocation = new File(nugetPluginDataDir, NUPKG);
    final File oldNuGetPackedToolsLocation = new File(nugetPluginDataDir, AGENT);
    final File oldNuGetUnPackedToolContentLocation = new File(nugetPluginDataDir, TOOLS);

    final File[] nupkgs = oldNuGetPackagesLocation.listFiles(FeedConstants.NUGET_TOOL_FILE_FILTER);
    if(nupkgs == null || nupkgs.length == 0){
      LOG.debug("No existing NuGet packages found on path " + oldNuGetPackagesLocation.getAbsolutePath());
    } else{
      for (final File oldLocation : nupkgs){
        final File newLocation = myToolPaths.getSharedToolPath(oldLocation);
        LOG.debug(String.format("Moving existing nupkg from %s to %s", oldLocation, newLocation));
        try {
          FileUtil.copy(oldLocation, newLocation);
          FileUtil.delete(oldLocation);
          LOG.debug(String.format("Succesfully moved nupkg from %s to %s", oldLocation, newLocation));
        } catch (IOException e) {
          throw new ToolException(String.format("Failed to move nupkg from %s to %s", oldLocation, newLocation), e);
        }
      }
    }

    FileUtil.delete(oldNuGetPackagesLocation);
    LOG.debug("Deleted directory " + oldNuGetPackagesLocation.getAbsolutePath());

    FileUtil.delete(oldNuGetPackedToolsLocation);
    LOG.debug("Deleted directory " + oldNuGetPackedToolsLocation.getAbsolutePath());

    FileUtil.delete(oldNuGetUnPackedToolContentLocation);
    LOG.debug("Deleted directory " + oldNuGetUnPackedToolContentLocation.getAbsolutePath());
  }
}
