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

package jetbrains.buildServer.nuget.agent.runner.pack;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FilesWatcher;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.runner.NuGetRunnerBase;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CompositeBuildProcess;
import jetbrains.buildServer.nuget.agent.util.MatchFilesBuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 12:11
 */
public class PackRunner extends NuGetRunnerBase {
  private final PackRunnerOutputDirectoryTracker myTracker;
  private final ArtifactsWatcher myPublisher;
  private final SmartDirectoryCleaner myCleaner;

  public PackRunner(@NotNull final NuGetActionFactory actionFactory,
                    @NotNull final PackagesParametersFactory parametersFactory,
                    @NotNull final PackRunnerOutputDirectoryTracker tracker,
                    @NotNull final ArtifactsWatcher publisher,
                    @NotNull final SmartDirectoryCleaner cleaner) {
    super(actionFactory, parametersFactory);
    myTracker = tracker;
    myPublisher = publisher;
    myCleaner = cleaner;
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    final CompositeBuildProcess process = new CompositeBuildProcessImpl();
    final NuGetPackParameters params = myParametersFactory.loadPackParameters(context);

    final FilesWatcher watcher = createFileWatcher(params.getOutputDirectory());

    process.pushBuildProcess(new OutputDirectoryCleanerProcess(params, runningBuild, myCleaner, myTracker.getState(runningBuild)));

    if (params.publishAsArtifacts()) {
      process.pushBuildProcess(resetFileWatcherProcess(watcher));
    }

    final CompositeBuildProcess packRunners = new CompositeBuildProcessImpl();

    process.pushBuildProcess(
            new MatchFilesBuildProcess(context, params, new MatchFilesBuildProcessBase.Callback() {
              public void fileFound(@NotNull File file) throws RunBuildException {
                packRunners.pushBuildProcess(myActionFactory.createPack(context, file, params));
              }
            })
    );
    process.pushBuildProcess(packRunners);

    if (params.publishAsArtifacts()) {
      process.pushBuildProcess(publishArtifactsProcess(runningBuild, watcher));
    }

    return process;
  }

  private BuildProcessBase resetFileWatcherProcess(@NotNull final FilesWatcher watcher) {
    return new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        watcher.resetChanged();
        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    };
  }

  private BuildProcessBase publishArtifactsProcess(@NotNull final AgentRunningBuild runningBuild,
                                                   @NotNull final FilesWatcher watcher) {
    return new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        final Set<File> allFiles = new TreeSet<File>();
        watcher.registerListener(new ChangeListener() {
          public void changeOccured(String requestor) {
            allFiles.addAll(watcher.getModifiedFiles());
            allFiles.addAll(watcher.getNewFiles());
          }
        });
        watcher.checkForModifications();

        LOG.warn("Created packages to publish as artifacts: " + allFiles);
        if (allFiles.isEmpty()) {
          runningBuild.getBuildLogger().warning("No new package files were created. Nothing to publish as artifacs.");
        } else {
          final StringBuilder sb = new StringBuilder();
          for (File file : allFiles) {
            sb.append(file.getPath()).append(" => .").append("\r\n");
          }
          myPublisher.addNewArtifactsPath(sb.toString());
        }
        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    };
  }

  private FilesWatcher createFileWatcher(final File outputDir) {
    return new FilesWatcher(new FilesWatcher.WatchedFilesProvider() {
      public File[] getWatchedFiles() {
        final File[] files = outputDir.listFiles();
        return files != null ? files : new File[0];
      }
    });
  }

  @NotNull
  public String getType() {
    return PackagesConstants.PACK_RUN_TYPE;
  }
}
