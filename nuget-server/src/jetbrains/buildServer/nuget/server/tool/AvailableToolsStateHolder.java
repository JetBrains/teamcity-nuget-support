

package jetbrains.buildServer.nuget.server.tool;

import jetbrains.buildServer.tools.available.*;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by Evgeniy.Koshkin on 17-Mar-16.
 */
public class AvailableToolsStateHolder implements AvailableToolsState {

  private final AvailableToolsState myAvailableTools;

  public AvailableToolsStateHolder(@NotNull TimeService timeService, @NotNull Collection<AvailableToolsFetcher> fetchers) {
    myAvailableTools = new AvailableToolsStateImpl(timeService, fetchers);
  }

  @Nullable
  @Override
  public DownloadableToolVersion findTool(@NotNull String version) {
    return myAvailableTools.findTool(version);
  }

  @NotNull
  @Override
  public FetchAvailableToolsResult getAvailable(FetchToolsPolicy fetchToolsPolicy) {
    return myAvailableTools.getAvailable(fetchToolsPolicy);
  }
}
