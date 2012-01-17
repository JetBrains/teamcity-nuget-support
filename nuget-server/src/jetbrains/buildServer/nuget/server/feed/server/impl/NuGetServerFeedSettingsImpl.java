/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.impl;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerDotNetSettingsEx;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerJavaSettings;
import jetbrains.buildServer.nuget.server.feed.server.dotNetFeed.MetadataControllersPaths;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsWriter;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static jetbrains.buildServer.nuget.server.feed.server.impl.UrlUtil.join;
import static jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent.SERVER;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 18:55
 */
public class NuGetServerFeedSettingsImpl implements NuGetServerDotNetSettingsEx, NuGetServerJavaSettings {
  private static final String NUGET_SERVER_MODE = "feed.enabled";
  private static final String NUGET_DOTNET_SERVER_URL = "feed.teamcity.url";

  private final RootUrlHolder myRootUrl;
  private final ServerPaths myPaths;
  private final NuGetSettingsManager mySettings;
  private final SystemInfo mySystemInfo;
  private final MetadataControllersPaths myController;

  public NuGetServerFeedSettingsImpl(@NotNull final RootUrlHolder rootUrl,
                                     @NotNull final MetadataControllersPaths controller,
                                     @NotNull final ServerPaths paths,
                                     @NotNull final NuGetSettingsManager settings,
                                     @NotNull final SystemInfo systemInfo) {
    myRootUrl = rootUrl;
    myController = controller;
    myPaths = paths;
    mySettings = settings;
    mySystemInfo = systemInfo;
  }

  public void setNuGetDotNetFeedEnabled(final boolean newValue) {
    setServerEnabled(ServerMode.DotNet);
  }

  public void setNuGetJavaFeedEnabled(final boolean newValue) {
    setServerEnabled(ServerMode.Java);
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
    return getServerMode() == ServerMode.Java;
  }

  public boolean isNuGetDotNetFeedEnabled() {
    return getServerMode() == ServerMode.DotNet;
  }

  private ServerMode getServerMode() {
    final ServerMode mode = mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, ServerMode>() {
      public ServerMode executeAction(@NotNull NuGetSettingsReader action) {
        return ServerMode.parse(action.getStringParameter(NUGET_SERVER_MODE));
      }
    });
    if (mode == ServerMode.DotNet && !mySystemInfo.canStartNuGetProcesses()) return ServerMode.Disabled;
    return mode;
  }

  public void setTeamCityBaseUrl(@NotNull final String url) {
    mySettings.writeSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        if (myRootUrl.getRootUrl().equals(url.trim())) {
          action.removeParameter(NUGET_DOTNET_SERVER_URL);
        } else {
          action.setStringParameter(NUGET_DOTNET_SERVER_URL, url);
        }
        return null;
      }
    });
  }

  public void setDefaultTeamCityBaseUrl() {
    mySettings.writeSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.removeParameter(NUGET_DOTNET_SERVER_URL);
        return null;
      }
    });
  }

  public String getCustomTeamCityBaseUrl() {
    return mySettings.readSettings(SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, String>() {
      public String executeAction(@NotNull NuGetSettingsReader action) {
        return action.getStringParameter(NUGET_DOTNET_SERVER_URL);
      }
    });
  }

  @NotNull
  public String getTeamCityBackBaseUrl() {
    return join(getActualRootUrl(), myController.getBasePath());
  }

  @NotNull
  public String getActualRootUrl() {
    String url = getCustomTeamCityBaseUrl();
    if (url == null) {
      return myRootUrl.getRootUrl();
    }
    return url;
  }

  @NotNull
  public File getLogFilePath() {
    return new File(myPaths.getLogsPath(), "teamcity-nuget-server.log");
  }
}
