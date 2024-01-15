

package jetbrains.buildServer.nuget.server.trigger.impl.settings;

import jetbrains.buildServer.buildTriggers.PolledBuildTrigger;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.Dates;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 14:01
 */
public class PackageCheckerSettingsImpl implements PackageCheckerSettings {

  public long getPackageCheckInterval() {
    return TeamCityProperties.getInteger("teamcity.nuget.packageCheckInterval", 5 * 60 * 1000);
  }

  public long getTriggerPollInterval() {
    //triggers are called every second, not faster
    return Math.max(1000, TeamCityProperties.getInteger("teamcity.nuget.packageTriggerPollInterval", PolledBuildTrigger.DEFAULT_POLL_TRIGGER_INTERVAL * 1000));
  }

  public long getPackageCheckRequestIdleRemoveInterval(long checkInterval) {
    //make sure package is not cleaned up before requests from trigger
    return 5 * Math.max(checkInterval, getTriggerPollInterval()) + Dates.hours(1);
  }

  public long getMinSleepInterval() {
    return getPackageCheckInterval() / 2;
  }

  public long getMaxSleepInterval() {
    return Math.min(3 * getPackageCheckInterval() / 2, getTriggerPollInterval() / 2);
  }

  public long getPackageSourceAvailabilityCheckInterval() {
    return TeamCityProperties.getLong("teamcity.nuget.trigger.packageSourceCheckInterval", 5 * getPackageCheckInterval());
  }

  public int getCheckerThreads() {
    return TeamCityProperties.getInteger("teamcity.nuget.trigger.pool", 4);
  }

  public boolean allowBulkMode() {
    return TeamCityProperties.getBooleanOrTrue("teamcity.nuget.trigger.bulkMode");
  }

  public int getMaxPackagesToQueryInBulk() {
    return TeamCityProperties.getInteger("teamcity.nuget.trigger.bulkMode.maxPackagesToQuery", 10);
  }
}
