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

package jetbrains.buildServer.nuget.server.toolRegistry.tab;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.RequestPermissionsChecker;
import jetbrains.buildServer.nuget.server.settings.SettingsSection;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.08.11 20:38
 */
public class InstalledToolsController extends BaseController implements SettingsSection {
  public static final String SETTINGS_PAGE_ID = "nuget-exe";
  private final String myPath;
  private final NuGetToolManager myToolsManager;
  private final InstallToolController myInstaller;
  private final PluginDescriptor myDescriptor;

  public InstalledToolsController(@NotNull final AuthorizationInterceptor auth,
                                  @NotNull final PermissionChecker checker,
                                  @NotNull final WebControllerManager web,
                                  @NotNull final NuGetToolManager toolsManager,
                                  @NotNull final InstallToolController installer,
                                  @NotNull final PluginDescriptor descriptor) {
    myToolsManager = toolsManager;
    myInstaller = installer;
    myDescriptor = descriptor;
    myPath = descriptor.getPluginResourcesPath("tool/nuget-server-tools.html");
    auth.addPathBasedPermissionsChecker(myPath, new RequestPermissionsChecker() {
      public void checkPermissions(@NotNull AuthorityHolder authorityHolder, @NotNull HttpServletRequest request) throws AccessDeniedException {
       checker.assertAccess(authorityHolder);
      }
    });
    web.registerController(myPath, this);
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  @NotNull
  public String getSectionId() {
    return SETTINGS_PAGE_ID;
  }

  @NotNull
  public String getSectionName() {
    return "NuGet Commandline";
  }

  @NotNull
  public String getIncludePath() {
    return getPath();
  }

  @NotNull
  public Collection<String> getCssFiles() {
    return Arrays.asList(myDescriptor.getPluginResourcesPath("tool/tools.css"));
  }

  @NotNull
  public Collection<String> getJsFiles() {
    return Arrays.asList(myDescriptor.getPluginResourcesPath("tool/tools.js"));
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("tool/tools.jsp"));
    mv.getModel().put("tools", getModel());
    mv.getModel().put("installerUrl", myInstaller.getPath());
    mv.getModel().put("updateUrl", this.getPath());
    mv.getModel().put("hasDefaultSelected", myToolsManager.getDefaultTool() != null);

    return mv;
  }

  @NotNull
  private Collection<NuGetInstalledTool> getModel() {
    return new ArrayList<NuGetInstalledTool>(myToolsManager.getInstalledTools());
  }
}
