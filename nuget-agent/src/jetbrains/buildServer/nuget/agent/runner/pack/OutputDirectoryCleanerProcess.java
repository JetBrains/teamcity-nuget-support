/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.12.11 14:35
 */
public class OutputDirectoryCleanerProcess extends BuildProcessBase {
  private static final Logger LOG = Logger.getInstance(OutputDirectoryCleanerProcess.class.getName());

  private final NuGetPackParameters myParams;
  private final AgentRunningBuild myRunningBuild;
  private final SmartDirectoryCleaner myCleaner;

  public OutputDirectoryCleanerProcess(@NotNull final NuGetPackParameters params,
                                       @NotNull final AgentRunningBuild runningBuild,
                                       @NotNull final SmartDirectoryCleaner cleaner) {
    myParams = params;
    myRunningBuild = runningBuild;
    myCleaner = cleaner;
  }

  @NotNull
  @Override
  protected BuildFinishedStatus waitForImpl() throws RunBuildException {
    final File output = myParams.getOutputDirectory();
    if(myParams.cleanOutputDirectory()){
      myRunningBuild.getBuildLogger().message("Cleaning output directory " + output.getAbsolutePath());
      LOG.info("Cleaning output directory " + output.getAbsolutePath());
      cleanFiles(output);
    }
    createOutputDirectory(output);
    return BuildFinishedStatus.FINISHED_SUCCESS;
  }

  private void createOutputDirectory(@NotNull final File output) throws RunBuildException {
    //noinspection ResultOfMethodCallIgnored
    output.mkdirs();
    if (!output.isDirectory()) {
      throw new RunBuildException("Failed to create output directory: " + output);
    }
  }

  private void cleanFiles(@NotNull final File output) throws RunBuildException {
    final CleanerCallback callback = new CleanerCallback(myRunningBuild.getBuildLogger());
    myCleaner.cleanFolder(output, callback);
    if (callback.isHasErrors()) {
      throw new RunBuildException("Failed to clean output directory: " + output);
    }
  }

  private class CleanerCallback implements SmartDirectoryCleanerCallback {
    private final BuildProgressLogger myLogger;
    private boolean myHasErrors = false;
    private File myRoot;

    public CleanerCallback(@NotNull final BuildProgressLogger logger) {
      myLogger = logger;
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
      log("Failed to clean all files under directory: " + problem.getAbsolutePath());
      myHasErrors = true;
    }

    private void log(String msg) {
      myLogger.error(msg);
      LOG.warn(msg);
    }

    public void logFailedToCleanFile(final File problem) {
      log("Failed to delete file: " + problem.getAbsolutePath());
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
