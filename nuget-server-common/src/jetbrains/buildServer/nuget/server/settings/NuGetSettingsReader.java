

package jetbrains.buildServer.nuget.server.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * NuGet Settings reader for dedicated component
 *
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:14
 * @see NuGetSettingsManager
 */
public interface NuGetSettingsReader {
  @Nullable
  String getStringParameter(@NotNull final String key);

  boolean getBooleanParameter(@NotNull final String key, boolean def);

  int getIntParameter(@NotNull final String key, int def);
}
