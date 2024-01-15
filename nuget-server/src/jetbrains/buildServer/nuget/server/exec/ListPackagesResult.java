

package jetbrains.buildServer.nuget.server.exec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created 03.01.13 17:43
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface ListPackagesResult {
  @Nullable
  String getErrorMessage();

  @NotNull
  Collection<SourcePackageInfo> getCollectedInfos();
}
