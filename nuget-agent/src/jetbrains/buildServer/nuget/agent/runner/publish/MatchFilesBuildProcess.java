/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.util.MatchFilesBuildProcessBase;
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
 * Date: 21.07.11 20:02
 */
public class MatchFilesBuildProcess extends MatchFilesBuildProcessBase {
  private final NuGetPublishParameters myParameters;

  public MatchFilesBuildProcess(@NotNull final BuildRunnerContext context,
                                @NotNull final NuGetPublishParameters parameters,
                                @NotNull final Callback callback) {
    super(context, callback);
    myParameters = parameters;
  }

  @NotNull
  @Override
  protected String getActionName() {
    return "create packages";
  }

  @NotNull
  @Override
  protected Collection<String> getFiles() throws RunBuildException {
    return myParameters.getFiles();

  }
}
