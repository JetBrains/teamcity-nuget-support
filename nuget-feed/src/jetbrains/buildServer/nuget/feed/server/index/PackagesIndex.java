

package jetbrains.buildServer.nuget.feed.server.index;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:17
 */
public interface PackagesIndex {
  @NotNull
  List<NuGetIndexEntry> getAll();

  @NotNull
  List<NuGetIndexEntry> getForBuild(long buildId);

  @NotNull
  List<NuGetIndexEntry> find(@NotNull Map<String, String> query);

  @NotNull
  List<NuGetIndexEntry> search(@NotNull Collection<String> keys, @NotNull String value);

  @NotNull
  List<NuGetIndexEntry> getByKey(String key);
}
