/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.runner.publish;

import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.runner.NuGetRunType;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static jetbrains.buildServer.nuget.common.PackagesConstants.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 14:15
 */
public class PublishRunType extends NuGetRunType {
  public PublishRunType(@NotNull final PluginDescriptor descriptor, @NotNull final NuGetToolManager toolManager) {
    super(descriptor, toolManager);
  }

  @NotNull
  @Override
  public String getType() {
    return PackagesConstants.PUBLISH_RUN_TYPE;
  }

  @Override
  public String getDisplayName() {
    return "NuGet Publish";
  }

  @Override
  public String getDescription() {
    return "Pushes and publishes NuGet package to a given feed";
  }

  @NotNull
  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new PropertiesProcessor() {
      public Collection<InvalidProperty> process(Map<String, String> properties) {
        final List<InvalidProperty> checks = new ArrayList<InvalidProperty>();

        if (StringUtil.isEmptyOrSpaces(properties.get(NUGET_PATH))) {
          checks.add(new InvalidProperty(NUGET_PATH, "The path to NuGet.exe must be specified"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(NUGET_PUBLISH_FILES))) {
          checks.add(new InvalidProperty(NUGET_PUBLISH_FILES, "Specify at least one package to pusblish"));
        }

        return checks;
      }
    };
  }

  @NotNull
  @Override
  protected String getEditJsp() {
    return "publish/editPublish.jsp";
  }

  @NotNull
  @Override
  protected String getViewJsp() {
    return "publish/viewPublish.jsp";
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return Collections.emptyMap();
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull Map<String, String> parameters) {
    StringBuilder sb = new StringBuilder();
    final String source = parameters.get(NUGET_PUBLISH_SOURCE);
    if (!StringUtil.isEmptyOrSpaces(source)) {
      sb.append("Publish to:").append(source).append("\n");
    }

    final String packages = parameters.get(NUGET_PUBLISH_FILES);
    if (!StringUtil.isEmptyOrSpaces(packages)) {
      sb.append("Packages: ");
      boolean isFirst = true;
      for (String split : packages.split("[\r\n]+")) {
        if (!StringUtil.isEmptyOrSpaces(split)) {
          if (!isFirst) {sb.append(", ");} else {isFirst = false; }
          sb.append(StringUtil.truncateStringValueWithDotsAtCenter(split, 50));
        }
      }
    }

    return sb.toString();
  }
}
