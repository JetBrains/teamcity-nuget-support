/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.tab;

import jetbrains.buildServer.nuget.feed.server.PermissionChecker;
import jetbrains.buildServer.web.openapi.*;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.08.11 20:35
 */
public class FeedServerSettingsTab extends SimpleCustomTab {
  public static final String TAB_ID = "nugetServerSettingsTab";
  private static final String NUGET = "NuGet Feed";

  @NotNull
  private final PluginDescriptor myPluginDescriptor;
  private final PermissionChecker myChecker;

  public FeedServerSettingsTab(@NotNull final PagePlaces pagePlaces,
                               @NotNull final PluginDescriptor pluginDescriptor,
                               @NotNull final PermissionChecker checker) {
    super(pagePlaces,
            PlaceId.ADMIN_SERVER_CONFIGURATION_TAB,
            TAB_ID,
            pluginDescriptor.getPluginResourcesPath("feedSettingsTab.jsp"),
            NUGET);
    myPluginDescriptor = pluginDescriptor;
    myChecker = checker;
    setPosition(PositionConstraint.between(Collections.singletonList("pluginsTab"), Arrays.asList("mavenSettings", "toolLoadTab", "usage-statistics")));
    addJsFile(pluginDescriptor.getPluginResourcesPath("feedServer.js"));
    addCssFile(pluginDescriptor.getPluginResourcesPath("feedServer.css"));
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
    model.put("includeUrl", myPluginDescriptor.getPluginResourcesPath("feed/status.html"));
  }
}
