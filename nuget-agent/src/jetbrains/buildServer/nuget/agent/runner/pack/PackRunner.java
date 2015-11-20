/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import com.intellij.util.Function;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.configuration.FilesState;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.runner.NuGetRunnerBase;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CompositeBuildProcess;
import jetbrains.buildServer.nuget.agent.util.MatchFilesBuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 12:11
 */
public class PackRunner extends NuGetRunnerBase {
  private final ArtifactsWatcher myPublisher;
  private final SmartDirectoryCleaner myCleaner;

  public PackRunner(@NotNull final NuGetActionFactory actionFactory,
                    @NotNull final PackagesParametersFactory parametersFactory,
                    @NotNull final ArtifactsWatcher publisher,
                    @NotNull final SmartDirectoryCleaner cleaner) {
    super(actionFactory, parametersFactory);
    myPublisher = publisher;
    myCleaner = cleaner;
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    final CompositeBuildProcess process = new CompositeBuildProcessImpl();
    final NuGetPackParameters params = myParametersFactory.loadPackParameters(context);

    final CompositeBuildProcess packRunners = new CompositeBuildProcessImpl();
    final Set<File> createdPackages = new TreeSet<File>();

    process.pushBuildProcess(new OutputDirectoryCleanerProcess(params, runningBuild, myCleaner));

    final FilesState filesState = createFilesState(params.getOutputDirectory());

    process.pushBuildProcess(
            new MatchFilesBuildProcess(context, params, new MatchFilesBuildProcessBase.Callback() {
              private File updateFile(@NotNull final File file) {
                if (params.preferProjectFileToNuSpec()) {
                  for (String ext : FeedConstants.NUGET_SUPPORTED_PROJECTS) {
                    final File projectFile = new File(file.getParentFile(), FileUtil.getNameWithoutExtension(file) +  ext);
                    if (projectFile.isFile()) {
                      runningBuild.getBuildLogger().message("Detected related project file for .nupkg file: " + projectFile);
                      return projectFile;
                    }
                  }
                }
                return file;
              }

              public void fileFound(@NotNull File file) throws RunBuildException {
                packRunners.pushBuildProcess(myActionFactory.createPack(context, updateFile(file), params));
              }
            })
    );
    //calls NuGet to pack all selected packages
    process.pushBuildProcess(packRunners);
    //collect changed files
    process.pushBuildProcess(collectCreatedFiles(filesState, createdPackages, params.getOutputDirectory()));
    //publish packages data
    process.pushBuildProcess(myActionFactory.createCreatedPackagesReport(context, createdPackages));
    //publish files as artifacts if needed
    if (params.publishAsArtifacts()) {
      process.pushBuildProcess(publishArtifactsProcess(runningBuild, createdPackages));
    }

    return process;
  }

  @NotNull
  private BuildProcessBase collectCreatedFiles(@NotNull final FilesState filesState,
                                               @NotNull final Set<File> detectedFiles,
                                               @NotNull final File outputDir) {
    return new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        filesState.computeChanges(getDirectoryFiles(outputDir));

        detectedFiles.clear();

        detectedFiles.addAll(filesState.getModifiedFiles());
        detectedFiles.addAll(filesState.getNewFiles());

        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    };
  }

  private BuildProcessBase publishArtifactsProcess(@NotNull final AgentRunningBuild runningBuild,
                                                   @NotNull final Set<File> createdPackages) {
    return new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        LOG.debug("Created packages to publish as artifacts: " + createdPackages);
        if (createdPackages.isEmpty()) {
          runningBuild.getBuildLogger().warning("No new package files were created. Nothing to publish as artifacs.");
        } else {
          runningBuild.getBuildLogger().message("Uploading created packages to build artifacts: " + filesList(createdPackages));
          if (runningBuild.isPersonal()) {
            runningBuild.getBuildLogger().warning("Packages from personal builds are not published to TeamCity NuGet Feed");
          }
          final StringBuilder sb = new StringBuilder();
          for (File file : createdPackages) {
            sb.append(file.getPath()).append(" => .").append("\r\n");
          }
          myPublisher.addNewArtifactsPath(sb.toString());
        }
        return BuildFinishedStatus.FINISHED_SUCCESS;
      }

      @NotNull
      private String filesList(@NotNull final Set<File> allFiles) {
        return StringUtil.join(allFiles, new Function<File, String>() {
          public String fun(File file) {
            return file.getName();
          }
        }, ", ");
      }
    };
  }

  @NotNull
  private FilesState createFilesState(@NotNull final File outputDir) {
    FilesState fs = new FilesState();
    fs.resetState(getDirectoryFiles(outputDir));
    return fs;
  }

  @NotNull
  private File[] getDirectoryFiles(@NotNull File dir) {
    File[] files = dir.listFiles();
    if (files == null) {
      files = new File[0];
    }
    return files;
  }

  @NotNull
  public String getType() {
    return PackagesConstants.PACK_RUN_TYPE;
  }
}
