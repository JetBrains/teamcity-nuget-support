

package jetbrains.buildServer.nuget.server.trigger.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.07.12 17:31
 * @since 7.1
 */
public interface PackageSourceChecker {
  /**
   * Checks NuGet source to be a valid network share path with list access
   * @param source source
   * @return null or error text
   */
  @Nullable
  String checkSource(@NotNull String source);
}
