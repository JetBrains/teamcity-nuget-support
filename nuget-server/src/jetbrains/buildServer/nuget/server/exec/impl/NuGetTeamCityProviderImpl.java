/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.exec.impl;

import jetbrains.buildServer.nuget.server.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.plugins.bean.PluginInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:47
 */
public class NuGetTeamCityProviderImpl implements NuGetTeamCityProvider {
  private final PluginInfo myPluginInfo;

  public NuGetTeamCityProviderImpl(@NotNull final PluginInfo pluginInfo) {
    myPluginInfo = pluginInfo;
  }

  @NotNull
  public File getNuGetRunnerPath() {
    return new File(myPluginInfo.getPluginRoot(), "bin/JetBrains.TeamCity.NuGetRunner.exe");
  }

  @NotNull
  public File getNuGetServerRunnerPath() {
    return new File(myPluginInfo.getPluginRoot(), "bin-server/JetBrains.TeamCity.NuGet.Server.exe");
  }

}
