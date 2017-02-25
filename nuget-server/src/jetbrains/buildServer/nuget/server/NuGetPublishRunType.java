/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created Eugene Petrenko (eugene.petrenko@gmail.com)
 * date: 28.04.11
 */
public class NuGetPublishRunType extends RunType {
  private final PluginDescriptor myPluginDescriptor;

  public NuGetPublishRunType(@NotNull final PluginDescriptor pluginDescriptor,
                             @NotNull final RunTypeRegistry registry) {
    myPluginDescriptor = pluginDescriptor;
    registry.registerRunType(this);
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return myPluginDescriptor.getPluginResourcesPath("editNuGet.jsp");
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return null;
  }

  @NotNull
  @Override
  public String getType() {
    return "NuGet";
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "NuGet Package";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Generates NuGet package from a given package specification (.nuspec) file";
  }

  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return null;
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return null;
  }
}
