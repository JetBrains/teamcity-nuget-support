

package jetbrains.buildServer.nuget.server.settings;

import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 17:52
 */
public interface NuGetSettingsEventHandler extends EventListener {
  void settingsChanged(@NotNull NuGetSettingsComponent component);
  void settingsReloaded();
}
