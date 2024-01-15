

package jetbrains.buildServer.nuget.server.trigger.impl.queue;

import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 18:14
 */
public interface PackageCheckQueue {
  long getSleepTime();

  @NotNull
  Collection<PackageCheckEntry> getItemsToCheckNow();

  void cleaupObsolete();
}
