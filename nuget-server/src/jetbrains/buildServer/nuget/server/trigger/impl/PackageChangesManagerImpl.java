

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.nuget.server.trigger.PackageChangesManager;
import jetbrains.buildServer.nuget.server.trigger.impl.queue.PackageCheckQueue;
import jetbrains.buildServer.nuget.server.trigger.impl.settings.PackageCheckerSettings;
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

    return Math.max(mySettings.getMinSleepInterval(), span);
  }

  @NotNull
  public Collection<PackageCheckEntry> getItemsToCheckNow() {
    final Collection<PackageCheckEntry> entries = new ArrayList<PackageCheckEntry>();
    final long now = myTimeService.now();

    synchronized (myEntries) {
      for (PackageCheckEntry entry : myEntries) {
        if (entry.isExecuting()) continue;
        //skip other mode
        if (entry.getNextCheckTime() < now) {
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

        //do not remove entry if result is not yet computed
        if (entry.getResult() == null) continue;
        if (entry.getRemoveTime() < now) {
          it.remove();
        }
      }
    }
  }
}
