

package jetbrains.buildServer.nuget.server.settings;

import org.jetbrains.annotations.NotNull;

/**
 * NuGet Settings writer interface for a compoenent
 *
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:14
 *
 * @see NuGetSettingsManager
 */
public interface NuGetSettingsWriter {
  void setStringParameter(@NotNull final String key, @NotNull String value);

  void setBooleanParameter(@NotNull final String key, boolean value);
  void setIntParameter(@NotNull final String key, int value);

  void removeParameter(@NotNull final String key);
}
