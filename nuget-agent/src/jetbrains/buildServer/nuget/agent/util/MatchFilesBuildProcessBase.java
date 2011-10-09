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

package jetbrains.buildServer.nuget.agent.util;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.util.fsScanner.DirectoryScanner;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 09.10.11 23:05
 */
public abstract class MatchFilesBuildProcessBase extends BuildProcessBase {
  protected final Logger LOG = Logger.getInstance(getClass().getName());
  private final BuildRunnerContext myContext;
  private final Callback myCallback;

  public MatchFilesBuildProcessBase(@NotNull final BuildRunnerContext context,
                                    @NotNull final Callback callback) {
    myContext = context;
    myCallback = callback;
  }

  @NotNull
  protected abstract Collection<String> getFiles() throws RunBuildException;

  @NotNull
  protected abstract String getActionName();

  @NotNull
  @Override
  protected final BuildFinishedStatus waitForImpl() throws RunBuildException {
    boolean found = false;

    final List<String> patterns = new ArrayList<String>();
    for (String _pattern : getFiles()) {
      String pattern = _pattern.trim();
      if (StringUtil.isEmptyOrSpaces(pattern)) {
        continue;
      }

      patterns.add(pattern);
    }

    final File root = myContext.getBuild().getCheckoutDirectory();
    try {
      final Collection<File> result = DirectoryScanner.findFiles(root, patterns, Collections.<String>emptyList());
      for (File file : result) {
        LOG.debug("Found file: " + file);
        found = true;
        myCallback.fileFound(file);
      }
    } catch (Exception e) {
      throw new RunBuildException("Failed to find files to " + getActionName() + ". " + e.getMessage(), e);
    }

    if (!found) {
      throw new RunBuildException("Failed to find files to " + getActionName() + " matching: " + new ArrayList<String>(getFiles()) + " under " + root + ".");
    }

    return BuildFinishedStatus.FINISHED_SUCCESS;
  }

  public static interface Callback {
    void fileFound(@NotNull final File file) throws RunBuildException;
  }
}
