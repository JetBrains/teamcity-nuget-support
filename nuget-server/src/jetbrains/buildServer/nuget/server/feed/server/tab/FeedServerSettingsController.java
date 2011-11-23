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

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.RequestPermissionsChecker;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettingsEx;
import jetbrains.buildServer.nuget.server.toolRegistry.tab.PermissionChecker;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.11.11 17:56
 */
public class FeedServerSettingsController extends BaseController {
  @NotNull private final NuGetServerRunnerSettingsEx mySettings;

  public FeedServerSettingsController(@NotNull final AuthorizationInterceptor auth,
                                      @NotNull final PermissionChecker checker,
                                      @NotNull final FeedServerSettingsSection section,
                                      @NotNull final WebControllerManager web,
                                      @NotNull final NuGetServerRunnerSettingsEx settings) {
    mySettings = settings;
    final String myPath = section.getSettingsPath();

    auth.addPathBasedPermissionsChecker(myPath, new RequestPermissionsChecker() {
      public void checkPermissions(@NotNull AuthorityHolder authorityHolder,
                                   @NotNull HttpServletRequest request) throws AccessDeniedException {
        checker.assertAccess(authorityHolder);
      }
    });
    web.registerController(myPath, this);
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {

    final Boolean param = getServerStatus(request);
    if (param == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }

    if (Boolean.TRUE.equals(param)) {
      final String url = request.getParameter("prop:" + FeedServerContants.NUGET_SERVER_URL);
      if (StringUtil.isEmptyOrSpaces(url)) {
        mySettings.setDefaultTeamCityBaseUrl();
      } else {
        mySettings.setTeamCityBaseUrl(url.trim());
      }
    }

    mySettings.setNuGetFeedEnabled(param);
    response.setStatus(HttpServletResponse.SC_OK);
    return null;
  }

  @Nullable
  private Boolean getServerStatus(@NotNull final HttpServletRequest request) {
    final String v = request.getParameter("prop:" + FeedServerContants.NUGET_SERVER_ENABLED_CHECKBOX);
    if (StringUtil.isEmptyOrSpaces(v)) return false;
    try {
      return Boolean.valueOf(v);
    } catch (Exception e) {
      return null;
    }
  }
}
