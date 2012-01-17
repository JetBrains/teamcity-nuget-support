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
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettingsEx;
import jetbrains.buildServer.nuget.server.feed.server.dotNetFeed.MetadataControllersPaths;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsWriter;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 18:55
 */
public class NuGetServerRunnerSettingsImpl implements NuGetServerRunnerSettingsEx {
  private static final String NUGET_SERVER_ENABLED = "feed.enabled";
  private static final String NUGET_SERVER_URL = "feed.teamcity.url";

  private final RootUrlHolder myRootUrl;
  private final ServerPaths myPaths;
  private final NuGetSettingsManager mySettings;
  private final SystemInfo mySystemInfo;
  private final MetadataControllersPaths myController;


  public NuGetServerRunnerSettingsImpl(@NotNull final RootUrlHolder rootUrl,
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

  public void setNuGetFeedEnabled(final boolean newValue) {
    mySettings.writeSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.setBooleanParameter(NUGET_SERVER_ENABLED, newValue);
        return null;
      }
    });
  }

  public void setTeamCityBaseUrl(@NotNull final String url) {
    mySettings.writeSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        if (myRootUrl.getRootUrl().equals(url.trim())) {
          action.removeParameter(NUGET_SERVER_URL);
        } else {
          action.setStringParameter(NUGET_SERVER_URL, url);
        }
        return null;
      }
    });
  }

  public void setDefaultTeamCityBaseUrl() {
    mySettings.writeSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.removeParameter(NUGET_SERVER_URL);
        return null;
      }
    });
  }

  public String getCustomTeamCityBaseUrl() {
    return mySettings.readSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, String>() {
      public String executeAction(@NotNull NuGetSettingsReader action) {
        return action.getStringParameter(NUGET_SERVER_URL);
      }
    });

  }

  public boolean isNuGetFeedEnabled() {
    if (!mySystemInfo.canStartNuGetProcesses()) return false;

    return mySettings.readSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Boolean>() {
      public Boolean executeAction(@NotNull NuGetSettingsReader action) {
        return action.getBooleanParameter(NUGET_SERVER_ENABLED, false);
      }
    });
  }

  @NotNull
  public String getPackagesControllerUrl() {
    final String basePath = myController.getBasePath();
    return join(getActualRootUrl(), basePath);
  }

  @NotNull
  public String getActualRootUrl() {
    String url = getCustomTeamCityBaseUrl();
    if (url == null) {
      return myRootUrl.getRootUrl();
    }
    return url;
  }

  private String join(@NotNull String url, @NotNull String basePath) {
    return StringUtil.trimEnd(url, "/") + "/" + StringUtil.trimStart(basePath, "/");
  }

  @NotNull
  public File getLogFilePath() {
    return new File(myPaths.getLogsPath(), "teamcity-nuget-server.log");
  }

  @NotNull
  public String getNuGetFeedControllerPath() {
    return "/app/nuget/v1/FeedService.svc";
  }

  @NotNull
  public String getNuGetHttpAuthFeedControllerPath() {
    return join(WebUtil.HTTP_AUTH_PREFIX, getNuGetFeedControllerPath());
  }

  @NotNull
  public String getNuGetGuestAuthFeedControllerPath() {
    return join(WebUtil.GUEST_AUTH_PREFIX, getNuGetFeedControllerPath());
  }
}
