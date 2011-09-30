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

import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.NamedDeamonThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 14:30
 */
public class PackageChangesCheckerThread {
  private final ScheduledExecutorService myExecutor = Executors.newScheduledThreadPool(TeamCityProperties.getInteger("teamcity.nuget.trigger.pool", 4), new NamedDeamonThreadFactory("NuGet triggers"));
  private final PackageCheckQueue myHolder;
  private final Collection<PackageChecker> myCheckers;

  public PackageChangesCheckerThread(@NotNull final PackageCheckQueue holder,
                                     @NotNull final Collection<PackageChecker> checkers) {
    myHolder = holder;
    myCheckers = checkers;
  }

  public void stopPackagesCheck() {
    myExecutor.shutdown();
  }

  public void startPackagesCheck() {
    myExecutor.submit(new Runnable() {
      public void run() {
        for (final PackageChecker checker : myCheckers) {
          final List<PackageCheckEntry> items = getMatchedItems(checker, myHolder.getItemsToCheckNow());
          if (items.size() > 0) {
            checker.update(myExecutor, items);
          }
        }

        myHolder.cleaupObsolete();
        myExecutor.schedule(this, myHolder.getSleepTime(), TimeUnit.MILLISECONDS);
      }

      @NotNull
      private List<PackageCheckEntry> getMatchedItems(@NotNull final PackageChecker checker,
                                                      @NotNull final Collection<PackageCheckEntry> toCheck) {
        final List<PackageCheckEntry> items = new ArrayList<PackageCheckEntry>();
        for (PackageCheckEntry entry : toCheck) {
          if (checker.accept(entry.getRequest())) {
            //mark as pending
            entry.setExecuting();
            items.add(entry);
          }
        }
        return items;
      }
    });
  }


}
