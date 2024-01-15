

package jetbrains.buildServer.nuget.server.settings.impl;

import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:42
 */
public abstract class ReaderBase implements NuGetSettingsReader {
  public boolean getBooleanParameter(@NotNull String key, boolean def) {
    try {
      final String s = getStringParameter(key);
      if (!StringUtil.isEmptyOrSpaces(s))
        return Boolean.valueOf(s);
    } catch (Exception e) {
      //NOP
    }
    return def;
  }

  public int getIntParameter(@NotNull String key, int def) {
    try {
      final String s = getStringParameter(key);
      if (!StringUtil.isEmptyOrSpaces(s))
        return Integer.parseInt(s);
    } catch (Exception e) {
      //NOP
    }
    return def;
  }
}
