

package jetbrains.buildServer.nuget.server.settings;

import org.jetbrains.annotations.NotNull;

/**
 * Generic settings manager
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 13:47
 */
public interface NuGetSettingsManager {
  /**
   * This method is called to read settings. Synchronious.
   * @param component component to read settings for
   * @param action action to be called for settings snapshot
   * @param <T> type parameter of return type from action
   * @return an object from action
   */
  <T> T readSettings(@NotNull final NuGetSettingsComponent component, @NotNull Func<NuGetSettingsReader, T> action);

  /**
   * This method is called to write settings. Synchronious.
   * @param component component to write settings for
   * @param action action to be called for settings snapshot
   * @param <T> type parameter of return type from action
   * @return an object from action
   */
  <T> T writeSettings(@NotNull final NuGetSettingsComponent component, @NotNull Func<NuGetSettingsWriter, T> action);

  /**
   * Registers listener for settings change
   * @param eventListener lister to add
   */
  void addListener(@NotNull final NuGetSettingsEventHandler eventListener);

  /**
   * Removes listener for settings change
   * @param eventListener listener to remove
   */
  void removeListener(@NotNull final NuGetSettingsEventHandler eventListener);

  /**
   * Action interface
   * @param <T> argument type
   * @param <R> return type
   */
  interface Func<T, R> {
    R executeAction(@NotNull T action);
  }
}
