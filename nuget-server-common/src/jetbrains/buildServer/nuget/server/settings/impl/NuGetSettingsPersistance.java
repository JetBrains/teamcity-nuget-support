

package jetbrains.buildServer.nuget.server.settings.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 15:30
 */
public class NuGetSettingsPersistance {
  private static final Logger LOG = Logger.getInstance(NuGetSettingsPersistance.class.getName());
  private final NuGetSettingsManagerConfiguration myConfig;

  public NuGetSettingsPersistance(@NotNull final NuGetSettingsManagerConfiguration config) {
    myConfig = config;
  }

  public void saveSettings(@NotNull final SettingsState state) throws IOException {
    final File file = myConfig.getNuGetConfigXml();
    final Element data = state.toXml();

    FileUtil.saveDocument(new Document(data), file);
  }

  @NotNull
  public SettingsState loadSettings() throws IOException {
    final File file = myConfig.getNuGetConfigXml();
    if (!file.isFile() || file.length() < 10) {
      LOG.debug("Configuration file for NuGet plugin is empty, will use default settings");
      return new SettingsState();
    }

    final Element element;
    try {
      element = FileUtil.parseDocument(file);
    } catch (JDOMException e) {
      throw new IOException(e);
    }
    return SettingsState.load(element);
  }
}
