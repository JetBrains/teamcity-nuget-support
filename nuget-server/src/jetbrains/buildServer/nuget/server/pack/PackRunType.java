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

package jetbrains.buildServer.nuget.server.pack;

import jetbrains.buildServer.agent.ServerProvidedProperties;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.08.11 21:05
 */
public class PackRunType extends RunType {
  private final PluginDescriptor myDescriptor;

  public PackRunType(@NotNull final PluginDescriptor descriptor) {
    myDescriptor = descriptor;
  }

  @NotNull
  @Override
  public String getType() {
    return PackagesConstants.PACK_RUN_TYPE;
  }

  @Override
  public String getDisplayName() {
    return "NuGet Packages Pack";
  }

  @Override
  public String getDescription() {
    return "Creates NuGet package from a given spec file";
  }

  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new PropertiesProcessor() {
      public Collection<InvalidProperty> process(Map<String, String> properties) {
        final ArrayList<InvalidProperty> result = new ArrayList<InvalidProperty>();
        return result;
      }
    };
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return myDescriptor.getPluginResourcesPath("pack/editPack.jsp");
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return myDescriptor.getPluginResourcesPath("pack/viewPack.jsp");
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return new HashMap<String, String>(){{
      put(PackagesConstants.NUGET_PACK_VERSION, "%" + ServerProvidedProperties.BUILD_NUMBER_PROP + "%");
    }};
  }
}
