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

package jetbrains.buildServer.nuget.server.feed.server.index;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.feed.server.NuGetFeedException;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRegister;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 20:30
 */
public class FinishedBuildIndexer {
  private static final Logger LOG = Logger.getInstance(FinishedBuildIndexer.class.getName());

  public FinishedBuildIndexer(@NotNull final NuGetServerRegister register,
                              @NotNull final EventDispatcher<BuildServerListener> event,
                              @NotNull final ExecutorServices services) {
    event.addListener(new BuildServerAdapter(){

      private void visitArtifacts(@NotNull final BuildArtifact artifact, @NotNull final List<BuildArtifact> packages) {
        if (!artifact.isDirectory()) {
          if (artifact.getName().endsWith(".nupkg")) {
            packages.add(artifact);
          }
          return;
        }

        for (BuildArtifact children : artifact.getChildren()) {
          visitArtifacts(children, packages);
        }
      }

      @Override
      public void buildFinished(final SRunningBuild build) {
        services.getNormalExecutorService().submit(new Runnable() {
          public void run() {
            final List<BuildArtifact> packages = new ArrayList<BuildArtifact>();
            visitArtifacts(build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getRootArtifact(), packages);

            if (packages.isEmpty()) return;

            for (BuildArtifact aPackage : packages) {
              try {
                register.registerPackage(build, aPackage.getRelativePath());
              } catch (NuGetFeedException e) {
                LOG.warn("Failed to index package: " + aPackage.getRelativePath() + " for buildId=" + build.getBuildId());
              }
            }
          }
        });
      }
    });
  }
}
