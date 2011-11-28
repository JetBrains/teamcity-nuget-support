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

package jetbrains.buildServer.nuget.server.feed.server.process;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 15:27
 */
public class NuGetServerCruiser {
  private static final Logger LOG = Logger.getInstance(NuGetServerCruiser.class.getName());

  public NuGetServerCruiser(@NotNull final NuGetServerRunner runner,
                            @NotNull final ExecutorServices executors,
                            @NotNull final NuGetServerCruiserTask task,
                            @NotNull final EventDispatcher<BuildServerListener> events) {
    events.addListener(new BuildServerAdapter(){
      private final AtomicBoolean myShutDown = new AtomicBoolean();

      @Override
      public void serverStartup() {
        final Runnable action = new Runnable() {
          public void run() {
            if (myShutDown.get()) return;

            try {
              task.checkNuGetServerState();
            } catch (Throwable t) {
              LOG.warn("Failed to check NuGet Feed Server state. " + t.getMessage(), t);
            }

            if (myShutDown.get()) return;
            executors.getNormalExecutorService().schedule(this, TeamCityProperties.getInteger("teamcity.nuget.server.ping.time", 5), TimeUnit.SECONDS);
          }
        };
        action.run();
      }

      @Override
      public void serverShutdown() {
        myShutDown.set(true);
        runner.stopServer();
      }
    });
  }
}
