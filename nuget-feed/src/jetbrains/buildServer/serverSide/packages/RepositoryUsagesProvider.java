package jetbrains.buildServer.serverSide.packages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface RepositoryUsagesProvider {

  /**
   * Internal type name for which could be provided usages.
   * @return see above
   */
  @NotNull
  String getType();

  /**
   * Returns the number of builds where used by repository.
   * @param repository for which will be collected list of usages.
   * @return see above.
   */
  @NotNull
  Long getUsagesCount(@NotNull final Repository repository);
}
