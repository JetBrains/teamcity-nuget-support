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
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.NuGetToolsInstaller;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolsWatcher;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.08.11 20:47
 */
public class NuGetToolsInstallerImpl implements NuGetToolsInstaller {
  private static final Logger LOG = Logger.getInstance(NuGetToolsInstallerImpl.class.getName());

  private final ToolPaths myToolPaths;
  private final ToolsWatcher myWatcher;

  public NuGetToolsInstallerImpl(@NotNull final ToolPaths toolPaths,
                                 @NotNull final ToolsWatcher watcher) {
    myToolPaths = toolPaths;
    myWatcher = watcher;
  }

  public void installNuGet(@NotNull final String toolFileName, @NotNull final File toolFile) throws ToolException {
    LOG.info("Start installing package " + toolFileName + " from file: " + toolFile);
    if (FeedConstants.PACKAGE_FILE_NAME_FILTER.accept(toolFileName)) {
      NuGetPackageValidationUtil.validatePackage(toolFile);
    }
    final File dest = new File(myToolPaths.getNuGetToolsPackages(), toolFileName);
    if (dest.isFile()) throw new ToolException("Tool package with name " + toolFileName + " already exists");
    try {
      FileUtil.copy(toolFile, dest);
      FileUtil.delete(toolFile);
    } catch (IOException e) {
      LOG.debug("Failed to copy downloaded package from " + toolFile + " to " + dest + ". " + e.getMessage(), e);
      throw new ToolException("Failed to copy downloaded package. " + e.getMessage());
    }
    myWatcher.checkNow();
  }
}
