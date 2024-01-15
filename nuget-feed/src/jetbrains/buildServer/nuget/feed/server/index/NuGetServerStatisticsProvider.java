

package jetbrains.buildServer.nuget.feed.server.index;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public interface NuGetServerStatisticsProvider {
  @NotNull
  Map<String, Long> getIndexStatistics();
}
