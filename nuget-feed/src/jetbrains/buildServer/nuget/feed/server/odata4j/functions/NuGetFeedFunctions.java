

package jetbrains.buildServer.nuget.feed.server.odata4j.functions;

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.edm.EdmFunctionImport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedFunctions {
  private final Map<String, NuGetFeedFunction> myAPIv2Functions = new HashMap<>();

  public NuGetFeedFunctions(@NotNull final NuGetFeed feed) {
    addFunction(new FindPackagesByIdFunction(feed));
    addFunction(new GetUpdatesFunction(feed));
    addFunction(new SearchFunction(feed));
  }

  private void addFunction(NuGetFeedFunction feedFunction) {
    myAPIv2Functions.put(feedFunction.getName(), feedFunction);
  }

  @Nullable
  public NuGetFeedFunction find(@NotNull final EdmFunctionImport name) {
    return myAPIv2Functions.get(name.getName());
  }

  public Iterable<? extends NuGetFeedFunction> getAll() {
    return Collections.unmodifiableCollection(myAPIv2Functions.values());
  }
}
