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
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 14:11
 */
public class PackageChangesManagerImpl implements PackageChangesManager {
  private static final long CHECK_THREASHOLD = 10 * 1000; //tasks precision in 10sec
  private static final long MAX_SLEEP_THREASHOLD = 5 * 60 * 1000; //check triggers every 5mins

  private final TimeService myTimeService;
  private final List<PackageCheckEntry> myEntries = new ArrayList<PackageCheckEntry>();

  public PackageChangesManagerImpl(@NotNull TimeService timeService) {
    myTimeService = timeService;
  }

  @Nullable
  public CheckResult checkPackage(@NotNull PackageCheckRequest request) {
    synchronized (myEntries) {
      final PackageCheckEntry entry = findEntry(request);
      if (entry != null) {
        entry.update(request);
        return entry.getResult();
      }

      myEntries.add(new PackageCheckEntry(request, myTimeService));
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
    long span = now * 2;

    synchronized (myEntries) {
      for (PackageCheckEntry entry : myEntries) {
        final long time = entry.getNextCheckTime();
        span = Math.min(time - now, span);
      }
    }

    return Math.min(Math.max(CHECK_THREASHOLD, span), MAX_SLEEP_THREASHOLD);
  }

  @NotNull
  public MultiMap<CheckRequestMode, PackageCheckEntry> getItemsToCheckNow() {
    final MultiMap<CheckRequestMode, PackageCheckEntry> entries = new MultiMap<CheckRequestMode, PackageCheckEntry>();
    final long now = myTimeService.now();

    synchronized (myEntries) {
      for (PackageCheckEntry entry : myEntries) {
        if (entry.isExecuting()) continue;
        //skip other mode
        if (entry.getNextCheckTime() - now < CHECK_THREASHOLD) {
          entries.putValue(entry.getMode(), entry);
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
