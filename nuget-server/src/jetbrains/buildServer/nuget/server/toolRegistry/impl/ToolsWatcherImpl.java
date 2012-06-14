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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FilesWatcher;
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 13:24
 */
public class ToolsWatcherImpl implements ToolsWatcher {
  private static final Logger LOG = Logger.getInstance(ToolsWatcherImpl.class.getName());

  private final ToolPaths myPaths;
  private final ToolPacker myPacker;
  private final ToolUnpacker myUnpacker;
  private final FilesWatcher myWatcher;

  public ToolsWatcherImpl(@NotNull final ToolPaths paths,
                          @NotNull final ToolPacker packer,
                          @NotNull final ToolUnpacker unpacker) {
    myPaths = paths;
    myPacker = packer;
    myUnpacker = unpacker;
    myWatcher = new FilesWatcher(new FilesWatcher.WatchedFilesProvider() {
      public File[] getWatchedFiles() {
        return myPaths.getNuGetToolsPackages().listFiles();
      }
    });
    myWatcher.registerListener(new ChangeListener() {
      public void changeOccured(String requestor) {
        ToolsWatcherImpl.this.onFilesChanged(
                myWatcher.getModifiedFiles(),
                myWatcher.getNewFiles(),
                myWatcher.getRemovedFiles());
      }
    });
    myWatcher.setSleepingPeriod(10000);
  }

  public void setSleepingPeriod(int time) {
    myWatcher.setSleepingPeriod(time);
  }

  public void start() {
    myWatcher.start();
  }

  public void dispose() {
    myWatcher.stop();
  }

  public void checkNow() {
    myWatcher.checkForModifications();
  }

  private void onFilesChanged(List<File> modified, List<File> added, List<File> removed) {
    for (File file : CollectionsUtil.join(modified, removed)) {
      removePackage(file, getAgentFile(file), getUnpackedFolder(file));
    }

    for (File file : CollectionsUtil.join(modified, added)) {
      installPackage(file, getAgentFile(file), getUnpackedFolder(file));
    }
  }

  private void installPackage(@NotNull final File file, File agentFile, File unpackedFolder) {
    try {
      FileUtil.createParentDirs(agentFile);
      FileUtil.createDir(unpackedFolder);

      myUnpacker.extractPackage(file, unpackedFolder);
      myPacker.packTool(agentFile, unpackedFolder);
    } catch (Throwable t) {
      LOG.warn("Failed to unpack nuget commandline: " + file);
      FileUtil.delete(unpackedFolder);
      FileUtil.delete(agentFile);
    }
  }

  private void removePackage(@NotNull final File file, File agentFile, File unpackedFolder) {
    LOG.info("Removing NuGet package: " + file.getName());
    FileUtil.delete(file);
    FileUtil.delete(agentFile);
    FileUtil.delete(unpackedFolder);
  }

  @NotNull
  private File getUnpackedFolder(@NotNull final File packages) {
    //here we could take a look into .nuspec to fetch version and name
    return new File(myPaths.getNuGetToolsPath(), packages.getName());
  }

  @NotNull
  private File getAgentFile(@NotNull final File packages) {
    //here we could take a look into .nuspec to fetch version and name
    return new File(myPaths.getNuGetToolsAgentPluginsPath(), packages.getName() + ".zip");
  }
}
