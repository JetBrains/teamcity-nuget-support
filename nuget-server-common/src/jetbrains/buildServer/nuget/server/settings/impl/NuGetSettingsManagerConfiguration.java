

package jetbrains.buildServer.nuget.server.settings.impl;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:36
 */
public interface NuGetSettingsManagerConfiguration {
  @NotNull
  File getNuGetConfigXml();
}
