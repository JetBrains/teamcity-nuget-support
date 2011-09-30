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

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.util.MultiMap;
import jetbrains.buildServer.util.NamedDeamonThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 14:30
 */
public class PackageChangesCheckerThread {
  private final ScheduledExecutorService myExecutor = Executors.newScheduledThreadPool(4, new NamedDeamonThreadFactory("NuGet triggers"));
  private final PackageChangesManagerImpl myHolder;

  public PackageChangesCheckerThread(@NotNull final PackageChangesManagerImpl holder) {
    myHolder = holder;
  }

  public void stopPackagesCheck() {
    myExecutor.shutdown();
  }

  public void startPackagesCheck() {
    myExecutor.submit(new Runnable() {
      public void run() {
        final MultiMap<CheckRequestMode,PackageCheckEntry> toCheck = myHolder.getItemsToCheckNow();
        for (final Map.Entry<CheckRequestMode, List<PackageCheckEntry>> entry : toCheck.entrySet()) {

          final CheckRequestMode mode = entry.getKey();
          final List<PackageCheckEntry> values = entry.getValue();

          for (PackageCheckEntry p : values) {
            p.setExecuting();
          }

          mode.checkForUpdates(myExecutor, values);
        }

        myHolder.cleaupObsolete();
        myExecutor.schedule(this, myHolder.getSleepTime(), TimeUnit.MILLISECONDS);
      }
    });
  }

}
