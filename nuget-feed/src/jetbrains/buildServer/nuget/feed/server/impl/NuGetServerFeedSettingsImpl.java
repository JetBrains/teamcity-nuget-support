/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.impl;

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetServerJavaSettings;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsWriter;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent.SERVER;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 18:55
 */
public class NuGetServerFeedSettingsImpl implements NuGetServerJavaSettings {
  private static final String NUGET_SERVER_MODE = "feed.enabled";

  private final NuGetSettingsManager mySettings;

  public NuGetServerFeedSettingsImpl(@NotNull final NuGetSettingsManager settings) {
    mySettings = settings;
  }

  public void setNuGetJavaFeedEnabled(final boolean newValue) {
    setServerEnabled(newValue ? ServerMode.Java : ServerMode.Disabled);
  }

  public boolean isFilteringByTargetFrameworkEnabled() {
    return TeamCityProperties.getBoolean(NuGetFeedConstants.PROP_NUGET_FEED_FILTER_TARGETFRAMEWORK);
  }

  private void setServerEnabled(@NotNull final ServerMode mode) {
    mySettings.writeSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.setStringParameter(NUGET_SERVER_MODE, mode.getValue());
        return null;
      }
    });
  }

  public boolean isNuGetJavaFeedEnabled() {
    return getServerMode() != ServerMode.Disabled;
  }

  private ServerMode getServerMode() {
    return mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, ServerMode>() {
      public ServerMode executeAction(@NotNull NuGetSettingsReader action) {
        return ServerMode.parse(action.getStringParameter(NUGET_SERVER_MODE));
      }
    });
  }
}
