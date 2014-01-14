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

package jetbrains.buildServer.nuget.agent.runner.install.impl;

import jetbrains.buildServer.nuget.agent.runner.install.InstallStages;
import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 23:40
 */
public class InstallStagesImpl implements InstallStages {
  private final BuildProcessContinuation myLocate;
  private final BuildProcessContinuation myInstall;
  private final BuildProcessContinuation myUpdate;
  private final BuildProcessContinuation myPostInstall;
  private final BuildProcessContinuation myReport;

  public InstallStagesImpl(@NotNull final BuildProcessContinuation host) {
    //order is significant
    myLocate = push(host);
    myInstall = push(host);
    myUpdate = push(host);
    myPostInstall = push(host);
    myReport = push(host);
  }

  private static BuildProcessContinuation push(@NotNull final BuildProcessContinuation proc) {
    CompositeBuildProcessImpl child = new CompositeBuildProcessImpl();
    proc.pushBuildProcess(child);
    return child;
  }

  @NotNull
  public BuildProcessContinuation getLocateStage() {
    return myLocate;
  }

  @NotNull
  public BuildProcessContinuation getInstallStage() {
    return myInstall;
  }

  @NotNull
  public BuildProcessContinuation getUpdateStage() {
    return myUpdate;
  }

  @NotNull
  public BuildProcessContinuation getPostUpdateStart() {
    return myPostInstall;
  }

  @NotNull
  public BuildProcessContinuation getReportStage() {
    return myReport;
  }
}
