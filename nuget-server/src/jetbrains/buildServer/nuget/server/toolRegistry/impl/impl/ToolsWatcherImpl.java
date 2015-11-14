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
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FilesWatcher;
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.InstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolsWatcher;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 13:24
 */
public class ToolsWatcherImpl implements ToolsWatcher {
  private static final Logger LOG = Logger.getInstance(ToolsWatcherImpl.class.getName());
  private static final int SLEEPING_PERIOD = 10000;

  private final ToolPaths myPaths;
  private final FilesWatcher myWatcher;
  private final InstalledToolsFactory myInstalledToolsFactory;
  private final ExecutorServices myExecutors;

  public ToolsWatcherImpl(@NotNull final ToolPaths paths,
                          @NotNull final InstalledToolsFactory installedToolsFactory,
                          @NotNull final ExecutorServices executors) {
    myPaths = paths;
    myInstalledToolsFactory = installedToolsFactory;
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
    myWatcher.setSleepingPeriod(SLEEPING_PERIOD);
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

  public void updateTool(@NotNull final InstalledTool tool) {
    myExecutors.getNormalExecutorService().submit(ExceptionUtil.catchAll("update installed tool", new Runnable() {
      public void run() {
        synchronized (ToolsWatcherImpl.this) {
          tool.install();
        }
      }
    }));
  }

  private synchronized void onFilesChanged(@NotNull final List<File> modified,
                                           @NotNull final List<File> added,
                                           @NotNull final List<File> removed) {
    for (File file : CollectionsUtil.join(modified, removed)) {
      final InstalledTool installedTool = myInstalledToolsFactory.createToolForPath(file);
      if(installedTool != null) installedTool.delete();
    }

    for (File file : CollectionsUtil.join(modified, added)) {
      final InstalledTool installedTool = myInstalledToolsFactory.createToolForPath(file);
      if(installedTool != null) installedTool.install();
    }
  }
}
