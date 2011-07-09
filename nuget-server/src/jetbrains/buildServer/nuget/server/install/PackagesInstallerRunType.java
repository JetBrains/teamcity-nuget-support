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

package jetbrains.buildServer.nuget.server.install;

import jetbrains.buildServer.nuget.common.DotNetConstants;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static jetbrains.buildServer.nuget.common.PackagesInstallerConstants.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 08.07.11 20:45
 */
public class PackagesInstallerRunType extends RunType {
  private final PluginDescriptor myDescriptor;

  public PackagesInstallerRunType(@NotNull final PluginDescriptor descriptor) {
    myDescriptor = descriptor;
  }

  @NotNull
  @Override
  public String getType() {
    return RUN_TYPE;
  }

  @Override
  public String getDisplayName() {
    return "NuGet packages installer";
  }

  @Override
  public String getDescription() {
    return "Installs missing NuGet packages";
  }

  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new PropertiesProcessor() {
      public Collection<InvalidProperty> process(Map<String, String> properties) {
        List<InvalidProperty> checks = new ArrayList<InvalidProperty>();

        if (StringUtil.isEmptyOrSpaces(properties.get(NUGET_PATH))) {
          checks.add(new InvalidProperty(NUGET_PATH, "Path to nuget.exe must be specified"));
        }

        String sln = properties.get(SLN_PATH);
        if (StringUtil.isEmptyOrSpaces(sln)) {
          checks.add(new InvalidProperty(NUGET_PATH, "Path to solution file should be specified"));
        }
        if (!sln.toLowerCase().endsWith(".sln")) {
          checks.add(new InvalidProperty(NUGET_PATH, "File extension must be .sln. Specify path to .sln file."));
        }

        return checks;
      }
    };
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull Map<String, String> parameters) {
    return getDescription() + "\nSolution: " + parameters.get(SLN_PATH);
  }

  @Override
  public List<Requirement> getRunnerSpecificRequirements(@NotNull Map<String, String> runParameters) {
    List<Requirement> list = new ArrayList<Requirement>(super.getRunnerSpecificRequirements(runParameters));
    list.add(new Requirement(DotNetConstants.DOT_NET_FRAMEWORK_4_x86, null, RequirementType.EXISTS));
    return list;
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return myDescriptor.getPluginResourcesPath("install/editInstallPackage.jsp");
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return myDescriptor.getPluginResourcesPath("install/viewInstallPackage.jsp");
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return new TreeMap<String, String>();
  }
}
