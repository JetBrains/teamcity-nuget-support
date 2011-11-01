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

package jetbrains.buildServer.nuget.server.feed.server.tab;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.controllers.RequestPermissionsChecker;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettingsEx;
import jetbrains.buildServer.nuget.server.toolRegistry.tab.PermissionChecker;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 26.10.11 19:21
 */
public class FeedServerController extends BaseController {
  @NotNull private final FeedServerSettingsSection mySection;
  @NotNull private final PluginDescriptor myDescriptor;
  @NotNull private final NuGetServerRunnerSettingsEx mySettings;
  @NotNull private final RootUrlHolder myRootUrl;

  public FeedServerController(@NotNull final AuthorizationInterceptor auth,
                              @NotNull final PermissionChecker checker,
                              @NotNull final FeedServerSettingsSection section,
                              @NotNull final WebControllerManager web,
                              @NotNull final PluginDescriptor descriptor,
                              @NotNull final NuGetServerRunnerSettingsEx settings,
                              @NotNull final RootUrlHolder rootUrl) {
    mySection = section;
    myDescriptor = descriptor;
    mySettings = settings;
    myRootUrl = rootUrl;
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
    if (isPost(request)) {
      return doPost(request, response);
    }

    return doGet(request, response);
  }

  private ModelAndView doPost(@NotNull final HttpServletRequest request,
                              @NotNull final HttpServletResponse response) {
    final Boolean param = getServerStatus(request);
    if (param == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }

    mySettings.setNuGetFeedEnabled(param);
    response.setStatus(HttpServletResponse.SC_OK);
    return null;
  }

  @Nullable
  private Boolean getServerStatus(@NotNull final HttpServletRequest request) {
    final String v = request.getParameter("prop:" + FeedServerContants.NUGET_SERVER_ENABLED_CHECKBOX);
    if (StringUtil.isEmptyOrSpaces(v)) return null;
    try {
      return Boolean.valueOf(v);
    } catch (Exception e) {
      return null;
    }
  }

  private ModelAndView doGet(@NotNull final HttpServletRequest request,
                             @NotNull final HttpServletResponse response) {

    final ModelAndView modelAndView = new ModelAndView(myDescriptor.getPluginResourcesPath("server/feedServerSettings.jsp"));
    final Map<String, String> properties = new HashMap<String, String>();
    if (mySettings.isNuGetFeedEnabled()) {
      properties.put(FeedServerContants.NUGET_SERVER_ENABLED_CHECKBOX, "checked");
    }

    modelAndView.getModel().put("propertiesBean", new BasePropertiesBean(properties));
    modelAndView.getModel().put("serverUrl", myRootUrl.getRootUrl());
    modelAndView.getModel().put("nugetStatusRefreshUrl", mySection.getIncludePath());

    return modelAndView;
  }

}
