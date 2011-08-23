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

package jetbrains.buildServer.nuget.server.runner.pack;

import jetbrains.buildServer.agent.ServerProvidedProperties;
import jetbrains.buildServer.nuget.common.DotNetConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.util.BasePropertiesProcessor;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static jetbrains.buildServer.nuget.common.PackagesConstants.*;

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

  @NotNull
  @Override
  public String describeParameters(@NotNull Map<String, String> parameters) {
    StringBuilder sb = new StringBuilder();

    sb.append("Pack: ").append(parameters.get(PackagesConstants.NUGET_PACK_SPEC_FILE)).append("\n");
    sb.append("Version: ").append(parameters.get(PackagesConstants.NUGET_PACK_VERSION)).append("\n");
    return sb.toString();
  }

  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new BasePropertiesProcessor() {
      @Override
      protected void checkProperties(@NotNull Map<String, String> map, @NotNull Collection<InvalidProperty> result) {
        notEmpty(NUGET_PATH, "Path to nuget.exe must be specified", map, result);
        notEmpty(NUGET_PACK_SPEC_FILE, "Package definition file must be specified", map, result);
        notEmpty(NUGET_PACK_OUTPUT_DIR, "Package creation output directory must be specified", map, result);
        final String version = notEmpty(NUGET_PACK_VERSION, "Version must be specified", map, result);
        if (version != null && !ReferencesResolverUtil.containsReference(version) && !version.matches("\\d+(\\.\\d+){0,3}")) {
          result.add(new InvalidProperty(NUGET_PACK_VERSION, "Version must be in assmebly version format: D[.D[.D[.D]]], i.e. 1.2.3 or 5.4.3.2"));
        }

        //TODO: check properties are well-formed
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

  @Override
  public List<Requirement> getRunnerSpecificRequirements(@NotNull Map<String, String> runParameters) {
    List<Requirement> list = new ArrayList<Requirement>(super.getRunnerSpecificRequirements(runParameters));
    list.add(new Requirement(DotNetConstants.DOT_NET_FRAMEWORK_4_x86, null, RequirementType.EXISTS));
    return list;
  }
}
