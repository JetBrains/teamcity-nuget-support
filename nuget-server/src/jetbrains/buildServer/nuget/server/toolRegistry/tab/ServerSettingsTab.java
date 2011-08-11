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

package jetbrains.buildServer.nuget.server.toolRegistry.tab;

import jetbrains.buildServer.web.openapi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.08.11 20:35
 */
public class ServerSettingsTab extends SimpleCustomTab {
  private final PermissionChecker myChecker;

  public ServerSettingsTab(@NotNull final PagePlaces pagePlaces,
                           @NotNull final PluginDescriptor descriptor,
                           @NotNull final InstalledToolsController controller,
                           @NotNull final PermissionChecker checker) {
    super(pagePlaces,
            PlaceId.ADMIN_SERVER_CONFIGURATION_TAB,
            "nugetServerSettingsTab",
            controller.getPath(),
            "NuGet");
    myChecker = checker;
    setPosition(PositionConstraint.last());
    addCssFile(descriptor.getPluginResourcesPath("tool/tools.css"));
    addJsFile(descriptor.getPluginResourcesPath("tool/tools.js"));
    register();
  }

  @Override
  public boolean isVisible() {
    return super.isVisible() && myChecker.hasAccess();
  }
}
