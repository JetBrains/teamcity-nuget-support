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

package jetbrains.buildServer.nuget.server.runner;

import jetbrains.buildServer.nuget.common.DotNetConstants;
import jetbrains.buildServer.nuget.server.tool.NuGetServerToolProvider;
import jetbrains.buildServer.nuget.server.version.Version;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementQualifier;
import jetbrains.buildServer.requirements.RequirementType;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.tools.ToolVersion;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_PATH;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 18:47
 */
public abstract class NuGetRunType extends RunType {
  private static final Version LOWEST_VERSION_REQUIRED_4_5_DOT_NET = new Version(2, 8, 0);
  private final PluginDescriptor myDescriptor;
  private final ServerToolManager myToolManager;
  private final ProjectManager myProjectManager;

  protected NuGetRunType(@NotNull final PluginDescriptor descriptor, @NotNull ServerToolManager toolManager, @NotNull ProjectManager projectManager) {
    myDescriptor = descriptor;
    myToolManager = toolManager;
    myProjectManager = projectManager;
  }

  @NotNull
  @Override
  public abstract String getType();

  @NotNull
  @Override
  public abstract String getDisplayName();

  @NotNull
  @Override
  public abstract String getDescription();

  @Override
  @NotNull
  public abstract PropertiesProcessor getRunnerPropertiesProcessor();

  @NotNull
  protected abstract String getEditJsp();

  @NotNull
  protected abstract String getViewJsp();

  @NotNull
  public final String getEditRunnerParamsJspFilePath() {
    return myDescriptor.getPluginResourcesPath(getEditJsp());
  }

  @Override
  @NotNull
  public String getViewRunnerParamsJspFilePath() {
    return myDescriptor.getPluginResourcesPath(getViewJsp());
  }

  @NotNull
  @Override
  public abstract String describeParameters(@NotNull Map<String, String> parameters);


  @NotNull
  @Override
  public List<Requirement> getRunnerSpecificRequirements(@NotNull Map<String, String> runParameters) {
    List<Requirement> list = new ArrayList<>(super.getRunnerSpecificRequirements(runParameters));
    final String nugetPath = runParameters.get(NUGET_PATH);
    if(nugetPath != null){
      final ToolVersion toolVersion = myToolManager.resolveToolVersionReference(NuGetServerToolProvider.NUGET_TOOL_TYPE, nugetPath, myProjectManager.getRootProject());
      if(toolVersion != null){
        final Version version = Version.valueOf(toolVersion.getVersion());
        if(version != null){
          if(version.compareTo(LOWEST_VERSION_REQUIRED_4_5_DOT_NET) >= 0)
            list.add(new Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(Mono|" + DotNetConstants.DOT_NET_FRAMEWORK + "(" + DotNetConstants.v4_5 + "|" + DotNetConstants.v4_5_1 + "|" + DotNetConstants.v4_5_2 + "|" + DotNetConstants.v4_6 + "|" + DotNetConstants.v4_6_1 + ")_x86)", null, RequirementType.EXISTS));
          else
            list.add(new Requirement("(Mono|\"" + DotNetConstants.DOT_NET_FRAMEWORK_4_x86 + ")", null, RequirementType.EXISTS));
        }
      }
    }
    return list;
  }
}
