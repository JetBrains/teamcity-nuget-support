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

package jetbrains.buildServer.nuget.server.publish;

import jetbrains.buildServer.nuget.common.DotNetConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_API_KEY;
import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_PATH;
import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_PUBLISH_FILES;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 14:15
 */
public class PublishRunType extends RunType {
  private final PluginDescriptor myDescriptor;

  public PublishRunType(@NotNull final PluginDescriptor descriptor) {
    myDescriptor = descriptor;
  }

  @NotNull
  @Override
  public String getType() {
    return PackagesConstants.PUBLISH_RUN_TYPE;
  }

  @Override
  public String getDisplayName() {
    return "NuGet Publish Packages";
  }

  @Override
  public String getDescription() {
    return "Pushes and publishes NuGet package to a given feed";
  }

  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new PropertiesProcessor() {
      public Collection<InvalidProperty> process(Map<String, String> properties) {
        final List<InvalidProperty> checks = new ArrayList<InvalidProperty>();

        if (StringUtil.isEmptyOrSpaces(properties.get(NUGET_PATH))) {
          checks.add(new InvalidProperty(NUGET_PATH, "Path to nuget.exe must be specified"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(NUGET_API_KEY))) {
          checks.add(new InvalidProperty(NUGET_API_KEY, "API key must be specified"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(NUGET_PUBLISH_FILES))) {
          checks.add(new InvalidProperty(NUGET_API_KEY, "Specify at least one package to pusblish"));
        }

        return checks;
      }
    };
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return myDescriptor.getPluginResourcesPath("publish/editPublish.jsp");
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return myDescriptor.getPluginResourcesPath("publish/viewPublish.jsp");
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return Collections.emptyMap();
  }

  @Override
  public List<Requirement> getRunnerSpecificRequirements(@NotNull Map<String, String> runParameters) {
    List<Requirement> list = new ArrayList<Requirement>(super.getRunnerSpecificRequirements(runParameters));
    list.add(new Requirement(DotNetConstants.DOT_NET_FRAMEWORK_4_x86, null, RequirementType.EXISTS));
    return list;
  }

}
