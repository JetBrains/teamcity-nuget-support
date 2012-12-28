package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 28.12.12 13:50
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetToolsSettings {
  private static final String DEFAULT_NUGET_KEY = "default-nuget";
  private final NuGetSettingsManager mySettings;

  public NuGetToolsSettings(@NotNull NuGetSettingsManager settings) {
    mySettings = settings;
  }

  public void setDefaultTool(@NotNull final String toolId) {
    mySettings.writeSettings(NuGetSettingsComponent.NUGET, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.setStringParameter(DEFAULT_NUGET_KEY, toolId);
        return null;
      }
    });
  }

  @Nullable
  public String getDefaultToolId() {
    return mySettings.readSettings(NuGetSettingsComponent.NUGET, new NuGetSettingsManager.Func<NuGetSettingsReader, String>() {
      public String executeAction(@NotNull NuGetSettingsReader action) {
        return action.getStringParameter(DEFAULT_NUGET_KEY);
      }
    });
  }
}
