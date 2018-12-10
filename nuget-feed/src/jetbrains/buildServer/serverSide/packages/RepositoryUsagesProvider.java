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
   * Returns the list of builds where used by repository.
   * @param repository for which will be collected list of usages.
   * @param count sets the maximum number of builds to return.
   * @return see above.
   */
  @NotNull
  List<Long> getUsages(@NotNull final Repository repository, @Nullable final Integer count);
}
