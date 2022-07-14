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

package jetbrains.buildServer.nuget.server.runner.pack;

import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.runner.NuGetRunType;
import jetbrains.buildServer.nuget.server.util.BasePropertiesProcessor;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static jetbrains.buildServer.nuget.common.PackagesConstants.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.08.11 21:05
 */
public class PackRunType extends NuGetRunType {
  public PackRunType(@NotNull final PluginDescriptor descriptor,
                     @NotNull final ServerToolManager toolManager,
                     @NotNull final ProjectManager projectManager) {
    super(descriptor, toolManager, projectManager);
  }

  @NotNull
  @Override
  public String getType() {
    return PackagesConstants.PACK_RUN_TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "NuGet Pack";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Creates a NuGet package from a given spec file";
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull Map<String, String> parameters) {
    StringBuilder sb = new StringBuilder();
    sb.append("Pack: ").append(parameters.get(PackagesConstants.NUGET_PACK_SPEC_FILE)).append("\n");
    final String version = parameters.get(PackagesConstants.NUGET_PACK_VERSION);
    if(version != null){
      sb.append("Version: ").append(version).append("\n");
    }
    return sb.toString();
  }

  @NotNull
  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new BasePropertiesProcessor() {
      @Override
      protected void checkProperties(@NotNull Map<String, String> map, @NotNull Collection<InvalidProperty> result) {
        notEmpty(NUGET_PATH, "The path to nuget.exe must be specified", map, result);
        notEmpty(NUGET_PACK_SPEC_FILE, "Package definition files must be specified", map, result);
        notEmpty(NUGET_PACK_OUTPUT_DIR, "The package creation output directory must be specified", map, result);
        final String version = map.get(NUGET_PACK_VERSION);

        if (version != null && !StringUtil.isEmptyOrSpaces(version)
                && !ReferencesResolverUtil.containsReference(version)
                && !version.matches("\\d+(\\.\\d+(-.*)?){1,3}")) {
          result.add(new InvalidProperty(NUGET_PACK_VERSION, "The version must be in the NuGet version format, i.e. 1.2.3 or 5.4.3.2"));
        }

        //TODO: check properties are well-formed
      }
    };
  }

  @NotNull
  @Override
  protected String getEditJsp() {
    return "pack/editPack.jsp";
  }

  @NotNull
  @Override
  protected String getViewJsp() {
    return "pack/viewPack.jsp";
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return NuGetPackRunnerDefaults.getRunnerProperties();
  }
}
