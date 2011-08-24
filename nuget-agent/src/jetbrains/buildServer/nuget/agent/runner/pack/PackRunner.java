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
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.runner.NuGetRunnerBase;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CompositeBuildProcess;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 12:11
 */
public class PackRunner extends NuGetRunnerBase {
  private final SmartDirectoryCleaner myCleaner;

  public PackRunner(@NotNull final NuGetActionFactory actionFactory,
                    @NotNull final PackagesParametersFactory parametersFactory,
                    @NotNull final SmartDirectoryCleaner cleaner) {
    super(actionFactory, parametersFactory);
    myCleaner = cleaner;
  }

  @NotNull
  public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild,
                                         @NotNull final BuildRunnerContext context) throws RunBuildException {
    final CompositeBuildProcess process = new CompositeBuildProcessImpl();
    final NuGetPackParameters params = myParametersFactory.loadPackParameters(context);

    process.pushBuildProcess(new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        final File output = params.getOutputDirectory();

        if (params.cleanOutputDirectory()) {
          final CleanerCallback callback = new CleanerCallback(runningBuild.getBuildLogger(), Logger.getLogger(getClass()));
          myCleaner.cleanFolder(output, callback);
          if (callback.isHasErrors()) {
            return BuildFinishedStatus.FINISHED_FAILED;
          }
        }

        //noinspection ResultOfMethodCallIgnored
        output.mkdirs();
        if (!output.isDirectory()) {
         runningBuild.getBuildLogger().error("Failed to create output directory: " + output);
          return BuildFinishedStatus.FINISHED_FAILED;
        }

        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    });

    process.pushBuildProcess(myActionFactory.createPack(context, params));
    return process;
  }

  @NotNull
  public String getType() {
    return PackagesConstants.PACK_RUN_TYPE;
  }

  private class CleanerCallback implements SmartDirectoryCleanerCallback {
    private final BuildProgressLogger myLogger;
    private final Logger LOG;
    private boolean myHasErrors = false;
    private File myRoot;

    public CleanerCallback(@NotNull final BuildProgressLogger logger, @NotNull final Logger LOG) {
      myLogger = logger;
      this.LOG = LOG;
    }

    public void logCleanStarted(final File f) {
      myRoot = f;
      myLogger.message("Cleaning " + f.getAbsolutePath());
      LOG.info("Cleaning " + f.getAbsolutePath());
    }

    public void logFailedToDeleteEmptyDirectory(final File f) {
      final String message = "Failed to delete empty directory: " + f.getAbsolutePath();
      if (f != myRoot) {
        myLogger.error(message);
      } else {
        myLogger.warning(message);
      }
      LOG.warn(message);
    }

    public void logFailedToCleanFilesUnderDirectory(final File problem) {
      myLogger.error("Failed to clean all files under directory: " + problem.getAbsolutePath());
      LOG.warn("Failed to clean all files under directory: " + problem.getAbsolutePath());
      myHasErrors = true;
    }

    public void logFailedToCleanFile(final File problem) {
      myLogger.error("Failed to delete file: " + problem.getAbsolutePath());
      LOG.warn("Failed to delete file: " + problem.getAbsolutePath());
      myHasErrors = true;
    }

    public void logFailedToCleanEntireFolder(final File file) {
      myHasErrors = true;
    }

    public boolean isHasErrors() {
      return myHasErrors;
    }
  }
}
