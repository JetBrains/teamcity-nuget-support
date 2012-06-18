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

package jetbrains.buildServer.nuget.agent.dependencies.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.nuget.common.PackageDependencies;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 22:59
 */
public class PackagesWatcher {
  private static final Logger LOG = Logger.getInstance(PackagesWatcher.class.getName());

  public PackagesWatcher(@NotNull final EventDispatcher<AgentLifeCycleListener> events,
                         @NotNull final NuGetPackagesCollectorEx collector,
                         @NotNull final PackagesInfoUploader uploader) {
    events.addListener(new AgentLifeCycleAdapter(){
      @Override
      public void buildStarted(@NotNull AgentRunningBuild runningBuild) {
        collector.removeAllPackages();
      }

      @Override
      public void beforeBuildFinish(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
        PackageDependencies packages = collector.getUsedPackages();
        if (packages.getUsedPackages().isEmpty()) return;

        try {
          uploader.uploadDepectedPackages(build, packages);
        } catch (IOException e) {
          LOG.warn("Failed to generate and upload list of used NuGet packages. " + e.getMessage(), e);
        }
      }

      @Override
      public void buildFinished(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
        collector.removeAllPackages();
      }
    });
  }
}
