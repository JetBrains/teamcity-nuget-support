

package jetbrains.buildServer.nuget.server.settings;

import jetbrains.buildServer.util.EventListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 17:53
 */
@EventListenerAdapter
public class NuGetSettingsEventAdapter implements NuGetSettingsEventHandler {
  public void settingsChanged(@NotNull NuGetSettingsComponent component) {
  }

  public void settingsReloaded() {
  }
}
