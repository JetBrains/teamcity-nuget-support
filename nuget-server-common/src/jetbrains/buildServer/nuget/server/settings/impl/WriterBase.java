

package jetbrains.buildServer.nuget.server.settings.impl;

import jetbrains.buildServer.nuget.server.settings.NuGetSettingsWriter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:44
 */
public abstract class WriterBase implements NuGetSettingsWriter {
  public void setBooleanParameter(@NotNull String key, boolean value) {
    setStringParameter(key, String.valueOf(value));
  }

  public void setIntParameter(@NotNull String key, int value) {
    setStringParameter(key, String.valueOf(value));
  }
}
