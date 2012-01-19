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

package jetbrains.buildServer.nuget.server.feed.server.controllers.requests;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.12.11 13:57
 */
public class RecentNuGetRequestsDumper {
  private static final Logger LOG = Logger.getInstance(RecentNuGetRequestsDumper.class.getName());

  public RecentNuGetRequestsDumper(@NotNull final RecentNuGetRequests requests,
                                   @NotNull final ExecutorServices services,
                                   @NotNull final EventDispatcher<BuildServerListener> events) {
    final Runnable command = new Runnable() {
      public void run() {
        final Collection<String> recentRequests = requests.getRecentRequests();
        if (recentRequests.isEmpty()) return;
        LOG.info("NuGet recent requests: " + recentRequests);
      }
    };
    final ScheduledFuture<?> future = services.getNormalExecutorService().scheduleWithFixedDelay(command, 60, 60, TimeUnit.MINUTES);

    events.addListener(new BuildServerAdapter(){
      @Override
      public void serverShutdown() {
        command.run();
        future.cancel(false);
      }
    });
  }
}
