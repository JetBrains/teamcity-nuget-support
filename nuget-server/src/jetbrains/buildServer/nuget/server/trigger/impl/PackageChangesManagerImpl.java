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

import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 14:11
 */
public class PackageChangesManagerImpl implements PackageChangesManager, PackageCheckQueue {
  private final TimeService myTimeService;
  private final PackageCheckerSettings mySettings;
  private final List<PackageCheckEntry> myEntries = new ArrayList<PackageCheckEntry>();

  public PackageChangesManagerImpl(@NotNull final TimeService timeService,
                                   @NotNull final PackageCheckerSettings settings) {
    myTimeService = timeService;
    mySettings = settings;
  }

  @Nullable
  public CheckResult checkPackage(@NotNull PackageCheckRequest request) {
    synchronized (myEntries) {
      final PackageCheckEntry entry = findEntry(request);
      if (entry != null) {
        entry.update(request);
        return entry.getResult();
      }

      myEntries.add(new PackageCheckEntry(request, myTimeService, mySettings));
      return null;
    }
  }

  @Nullable
  private PackageCheckEntry findEntry(@NotNull final PackageCheckRequest request) {
    for (PackageCheckEntry entry : myEntries) {
      if (entry.forRequest(request)) return entry;
    }
    return null;
  }

  public long getSleepTime() {
    final long now = myTimeService.now();
    long span = mySettings.getMaxSleepInterval();

    synchronized (myEntries) {
      for (PackageCheckEntry entry : myEntries) {
        span = Math.min(entry.getNextCheckTime() - now, span);
      }
    }

    return Math.max(mySettings.getCheckChangesThreashold(), span);
  }

  @NotNull
  public Collection<PackageCheckEntry> getItemsToCheckNow() {
    final Collection<PackageCheckEntry> entries = new ArrayList<PackageCheckEntry>();
    final long now = myTimeService.now();

    synchronized (myEntries) {
      for (PackageCheckEntry entry : myEntries) {
        if (entry.isExecuting()) continue;
        //skip other mode
        if (entry.getNextCheckTime() - now < mySettings.getCheckChangesThreashold()) {
          entries.add(entry);
        }
      }
    }

    return entries;
  }

  public void cleaupObsolete() {
    final long now = myTimeService.now();

    synchronized (myEntries) {
      for (Iterator<PackageCheckEntry> it = myEntries.iterator(); it.hasNext(); ) {
        PackageCheckEntry entry = it.next();

        if (entry.getRemoveTime() < now) it.remove();
      }
    }
  }
}
