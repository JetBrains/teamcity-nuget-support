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

package jetbrains.buildServer.nuget.server.toolRegistry.impl.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FilesWatcher;
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.*;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.ExceptionUtil;
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
  private final PluginNaming myNaming;
  private final ExecutorServices myExecutors;

  public ToolsWatcherImpl(@NotNull final ToolPaths paths,
                          @NotNull final PluginNaming naming,
                          @NotNull final ToolPacker packer,
                          @NotNull final ToolUnpacker unpacker,
                          @NotNull final ExecutorServices executors) {
    myPaths = paths;
    myPacker = packer;
    myUnpacker = unpacker;
    myNaming = naming;
    myExecutors = executors;

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

  public void updatePackage(@NotNull final InstalledTool nupkg) {
    myExecutors.getNormalExecutorService().submit(ExceptionUtil.catchAll("update installed package", new Runnable() {
      public void run() {
        synchronized (ToolsWatcherImpl.this) {
          installPackage(nupkg);
        }
      }
    }));
  }

  private synchronized void onFilesChanged(@NotNull final List<File> modified,
                                           @NotNull final List<File> added,
                                           @NotNull final List<File> removed) {
    for (File file : CollectionsUtil.join(modified, removed)) {
      removePackage(new InstalledToolImpl(myNaming, file));
    }

    for (File file : CollectionsUtil.join(modified, added)) {
      installPackage(new InstalledToolImpl(myNaming, file));
    }
  }

  private void installPackage(@NotNull final InstalledTool file) {
    try {
      file.removeUnpackedFiles();

      FileUtil.createParentDirs(file.getAgentPluginFile());
      FileUtil.createDir(file.getUnpackFolder());
      myUnpacker.extractPackage(file.getPackageFile(), file.getUnpackFolder());
      myPacker.packTool(file.getAgentPluginFile(), file.getUnpackFolder());
    } catch (Throwable t) {
      LOG.warn("Failed to unpack nuget commandline: " + file);
      file.removeUnpackedFiles();
    }
  }

  private void removePackage(@NotNull final InstalledTool file) {
    file.delete();
  }
}
