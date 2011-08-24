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

package jetbrains.buildServer.nuget.agent.runner.publish;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.util.AntPatternFileFinder;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 20:02
 */
public class MatchFilesBuildProcess extends BuildProcessBase {
  private static final Logger LOG = Logger.getInstance(MatchFilesBuildProcess.class.getName());

  private final BuildRunnerContext myContext;
  private final NuGetPublishParameters myParameters;
  private final Callback myCallback;

  public MatchFilesBuildProcess(@NotNull final BuildRunnerContext context,
                                @NotNull final NuGetPublishParameters parameters,
                                @NotNull final Callback callback) {
    myContext = context;
    myParameters = parameters;
    myCallback = callback;
  }

  @NotNull
  @Override
  protected BuildFinishedStatus waitForImpl() throws RunBuildException {

    boolean found = false;

    final List<String> patterns = new ArrayList<String>();
    for (String _pattern : myParameters.getFiles()) {
      final String pattern = _pattern.trim();
      if (StringUtil.isEmptyOrSpaces(pattern)) {
        continue;
      }

      final File file = new File(pattern);
      if (file.isAbsolute()) {
        found = true;
        LOG.debug("Found .nugkg to push: " + file);
        myCallback.fileFound(file);
        continue;
      }

      patterns.add(pattern.replace('\\', '/'));
    }

    final String[] includes = patterns.toArray(new String[patterns.size()]);
    AntPatternFileFinder finder = new AntPatternFileFinder(
            includes,
            new String[0],
            SystemInfo.isFileSystemCaseSensitive
    );

    final File root = myContext.getBuild().getCheckoutDirectory();
    try {
      final File[] result = finder.findFiles(root);

      for (File file : result) {
        LOG.debug("Found nugkg to push: " + file);
        found = true;
        myCallback.fileFound(file);
      }
    } catch (IOException e) {
      throw new RunBuildException("Failed to find packages to publish. " + e.getMessage(), e);
    }

    if (!found) {
      throw new RunBuildException("Failed to find files to publish matching: " + new ArrayList<String>(myParameters.getFiles()) + " under " + root + ". No packages to publish. ");
    }

    return BuildFinishedStatus.FINISHED_SUCCESS;
  }

  public static interface Callback {
    void fileFound(@NotNull final File file) throws RunBuildException;
  }
}
