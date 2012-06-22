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

package jetbrains.buildServer.nuget.agent.commands.impl;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 20:27
 */
public class NuGetWorkdirCleaner {
  public NuGetWorkdirCleaner(@NotNull final NuGetWorkdirCalculatorImpl cal,
                             @NotNull final EventDispatcher<AgentLifeCycleListener> events) {
    events.addListener(new AgentLifeCycleAdapter(){
      private void cleanNuGetWorkDir(@NotNull AgentRunningBuild runningBuild) {

        String clean = runningBuild.getSharedConfigParameters().get("teamcity.nuget.clean.nugetWork");
        if (clean == null || !"true".equalsIgnoreCase(clean)) {
          return;
        }

        File dir = cal.getNuGetWorkDir(runningBuild);
        if (dir != null) {
          FileUtil.delete(dir);
        }
      }

      @Override
      public void buildStarted(@NotNull AgentRunningBuild runningBuild) {
        cleanNuGetWorkDir(runningBuild);
      }

      @Override
      public void buildFinished(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
        cleanNuGetWorkDir(build);
      }
    });
  }
}
