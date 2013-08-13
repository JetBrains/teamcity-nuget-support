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

package jetbrains.buildServer.nuget.server.runner;

import jetbrains.buildServer.nuget.common.DotNetConstants;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 18:47
 */
public abstract class NuGetRunType extends RunType {
  private final PluginDescriptor myDescriptor;

  protected NuGetRunType(@NotNull final PluginDescriptor descriptor) {
    myDescriptor = descriptor;
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


  @Override
  public List<Requirement> getRunnerSpecificRequirements(@NotNull Map<String, String> runParameters) {
    List<Requirement> list = new ArrayList<Requirement>(super.getRunnerSpecificRequirements(runParameters));
    list.add(new Requirement(DotNetConstants.DOT_NET_FRAMEWORK_4_x86, null, RequirementType.EXISTS));
    return list;
  }
}
