

package jetbrains.buildServer.nuget.server.trigger.impl.settings;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 20:27
 */
public interface PackageCheckerSettings {

  long getTriggerPollInterval();

  long getPackageCheckInterval();

  long getPackageCheckRequestIdleRemoveInterval(long checkInterval);

  long getMinSleepInterval();

  long getMaxSleepInterval();

  long getPackageSourceAvailabilityCheckInterval();

  int getCheckerThreads();

  boolean allowBulkMode();

  int getMaxPackagesToQueryInBulk();
}
