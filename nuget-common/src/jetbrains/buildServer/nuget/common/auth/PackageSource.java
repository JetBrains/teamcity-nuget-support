

package jetbrains.buildServer.nuget.common.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents NuGet package source
 * Created 04.01.13 19:12
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface PackageSource {
  @NotNull
  String getSource();

  @Nullable
  String getUsername();

  @Nullable
  String getPassword();
}
