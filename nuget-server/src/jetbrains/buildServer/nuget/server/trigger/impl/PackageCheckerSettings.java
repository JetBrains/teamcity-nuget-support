package jetbrains.buildServer.nuget.server.trigger.impl;

import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 20:27
 */
public interface PackageCheckerSettings {

  int getTriggerPollInterval();

  long getPackageCheckInterval();

  long getPackageCheckRequestIdleRemoveInterval(long checkInterval);

  long getMinSleepInterval();

  long getMaxSleepInterval();

  int getCheckerThreads();

  boolean allowBulkMode(@NotNull PackageCheckRequest request);

  int getMaxPackagesToQueryInBulk();
}
