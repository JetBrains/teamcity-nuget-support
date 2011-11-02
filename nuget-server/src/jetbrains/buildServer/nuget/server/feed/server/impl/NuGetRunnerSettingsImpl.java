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
import jetbrains.buildServer.nuget.server.feed.server.controllers.MetadataControllersPaths;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsWriter;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 18:55
 */
public class NuGetRunnerSettingsImpl implements NuGetServerRunnerSettingsEx {
  private static final String NUGET_SERVER_ENABLED = "feed.enabled";

  private final RootUrlHolder myRootUrl;
  private final ServerPaths myPaths;
  private final NuGetSettingsManager mySettings;
  private final MetadataControllersPaths myController;
  private final String myAccessToken;


  public NuGetRunnerSettingsImpl(@NotNull final RootUrlHolder rootUrl,
                                 @NotNull final MetadataControllersPaths controller,
                                 @NotNull final ServerPaths paths,
                                 @NotNull final NuGetSettingsManager settings) {
    myRootUrl = rootUrl;
    myController = controller;
    myPaths = paths;
    mySettings = settings;
    myAccessToken = "jonnyzzz" + StringUtil.generateUniqueHash() + "rulezz";
  }

  public void setNuGetFeedEnabled(final boolean newValue) {
    mySettings.writeSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsWriter, Object>() {
      public Object executeAction(@NotNull NuGetSettingsWriter action) {
        action.setBooleanParameter(NUGET_SERVER_ENABLED, newValue);
        return null;
      }
    });
  }

  public boolean isNuGetFeedEnabled() {
    return mySettings.readSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, Boolean>() {
      public Boolean executeAction(@NotNull NuGetSettingsReader action) {
        return action.getBooleanParameter(NUGET_SERVER_ENABLED, false);
      }
    });
  }

  @NotNull
  public String getPackagesControllerUrl() {
    return mySettings.readSettings(NuGetSettingsComponent.SERVER, new NuGetSettingsManager.Func<NuGetSettingsReader, String>() {
      public String executeAction(@NotNull NuGetSettingsReader action) {
        final String url = action.getStringParameter("url");
        if (url != null) return url;
        return myRootUrl.getRootUrl() + myController.getBasePath();
      }
    });
  }

  @NotNull
  public File getLogsPath() {
    return myPaths.getLogsPath();
  }

  @NotNull
  public String getAccessTokenHeaderName() {
    return "X-TeamCity-HostId";
  }

  @NotNull
  public String getAccessToken() {
    return myAccessToken;
  }
}
