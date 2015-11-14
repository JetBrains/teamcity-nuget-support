/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.common.ToolIdUtils;
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
    return ToolIdUtils.normalizeToolId(mySettings.readSettings(NuGetSettingsComponent.NUGET, new NuGetSettingsManager.Func<NuGetSettingsReader, String>() {
      public String executeAction(@NotNull NuGetSettingsReader action) {
        return action.getStringParameter(DEFAULT_NUGET_KEY);
      }
    }));
  }
}
