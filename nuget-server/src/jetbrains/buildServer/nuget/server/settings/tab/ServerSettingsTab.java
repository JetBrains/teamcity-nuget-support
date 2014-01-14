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

package jetbrains.buildServer.nuget.server.settings.tab;

import jetbrains.buildServer.nuget.server.settings.SettingsSection;
import jetbrains.buildServer.nuget.server.toolRegistry.tab.PermissionChecker;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.*;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.08.11 20:35
 */
public class ServerSettingsTab extends SimpleCustomTab {
  public static final String TAB_ID = "nugetServerSettingsTab";
  private final PermissionChecker myChecker;
  private final Collection<SettingsSection> mySections = new ArrayList<SettingsSection>();

  public ServerSettingsTab(@NotNull final PagePlaces pagePlaces,
                           @NotNull final PluginDescriptor descriptor,
                           @NotNull final PermissionChecker checker,
                           @NotNull final Collection<SettingsSection> sections) {
    super(pagePlaces,
            PlaceId.ADMIN_SERVER_CONFIGURATION_TAB,
            TAB_ID,
            descriptor.getPluginResourcesPath("settings.jsp"),
            "NuGet Settings");
    myChecker = checker;
    setPosition(PositionConstraint.between(Arrays.asList("pluginsTab"), Arrays.asList("mavenSettings", "toolLoadTab", "usage-statistics")));

    for (SettingsSection section : sections) {
      for (String css : section.getCssFiles()) {
        addCssFile(css);
      }
      for (String css : section.getJsFiles()) {
        addJsFile(css);
      }
      mySections.add(section);
    }
    register();
  }

  @Override
  public boolean isVisible() {
    return super.isVisible() && myChecker.hasAccess();
  }

  @Override
  public boolean isAvailable(@NotNull HttpServletRequest request) {
    return super.isAvailable(request) && myChecker.hasAccess();
  }

  @Override
  public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
    super.fillModel(model, request);

    model.put("nuget_teamcity_include_controllers", mySections);
    model.put("nuget_teamcity_include_selected", getSelectedSection(request));
    model.put("nuget_teamcity_include_key", SettingsSection.SELECTED_SECTION_KEY);
  }

  @NotNull
  private SettingsSection getSelectedSection(@NotNull final HttpServletRequest request) {
    final String parameter = request.getParameter(SettingsSection.SELECTED_SECTION_KEY);

    if (!StringUtil.isEmptyOrSpaces(parameter)) {
      for (SettingsSection section : mySections) {
        if (section.getSectionId().equals(parameter)) return section;
      }
    }
    return mySections.iterator().next();
  }
}
