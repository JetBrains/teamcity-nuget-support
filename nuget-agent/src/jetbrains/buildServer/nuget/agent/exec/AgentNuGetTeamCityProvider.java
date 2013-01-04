/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.agent.exec;

import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProviderBase;
import org.jetbrains.annotations.NotNull;

/**
 * Created 04.01.13 14:51
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class AgentNuGetTeamCityProvider extends NuGetTeamCityProviderBase implements NuGetTeamCityProvider {
  public AgentNuGetTeamCityProvider(@NotNull final PluginDescriptor pluginInfo) {
    super(pluginInfo.getPluginRoot());
  }
}
