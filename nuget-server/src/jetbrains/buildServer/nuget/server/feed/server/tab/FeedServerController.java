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

package jetbrains.buildServer.nuget.server.feed.server.tab;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.RequestPermissionsChecker;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.server.toolRegistry.tab.PermissionChecker;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.impl.ServerSettings;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 26.10.11 19:21
 */
public class FeedServerController extends BaseController {
  @NotNull private final FeedServerSettingsSection mySection;
  @NotNull private final PluginDescriptor myDescriptor;
  @NotNull private final NuGetServerSettings mySettings;
  @NotNull private final ServerSettings myServerSettings;

  public FeedServerController(@NotNull final AuthorizationInterceptor auth,
                              @NotNull final PermissionChecker checker,
                              @NotNull final FeedServerSettingsSection section,
                              @NotNull final WebControllerManager web,
                              @NotNull final PluginDescriptor descriptor,
                              @NotNull final ServerSettings serverSettings,
                              @NotNull final NuGetServerSettings settings) {
    mySection = section;
    myDescriptor = descriptor;
    mySettings = settings;
    myServerSettings = serverSettings;
    final String myPath = section.getIncludePath();

    auth.addPathBasedPermissionsChecker(myPath, new RequestPermissionsChecker() {
      public void checkPermissions(@NotNull AuthorityHolder authorityHolder, @NotNull HttpServletRequest request) throws AccessDeniedException {
        checker.assertAccess(authorityHolder);
      }
    });
    web.registerController(myPath, this);
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    final ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("server/feedServerSettingsWindows.jsp"));

    mv.getModel().put("actualServerUrl", WebUtil.getRootUrl(request));
    mv.getModel().put("nugetStatusRefreshUrl", mySection.getIncludePath());
    mv.getModel().put("nugetSettingsPostUrl", mySection.getSettingsPath());
    mv.getModel().put("privateFeedUrl", mySettings.getNuGetHttpAuthFeedControllerPath());
    mv.getModel().put("publicFeedUrl", mySettings.getNuGetGuestAuthFeedControllerPath());
    mv.getModel().put("serverEnabled", mySettings.isNuGetServerEnabled());
    mv.getModel().put("isGuestEnabled", myServerSettings.isGuestLoginAllowed());

    return mv;
  }

}
