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

package jetbrains.buildServer.nuget.feed.server.tab;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.RequestPermissionsChecker;
import jetbrains.buildServer.nuget.feed.server.NuGetServerJavaSettings;
import jetbrains.buildServer.nuget.feed.server.index.NuGetPackagesIndexer;
import jetbrains.buildServer.nuget.server.PermissionChecker;
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

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.11.11 17:56
 */
public class FeedServerSettingsController extends BaseController {
  private static final String NUGET_FEED_ENABLED_PARAM_NAME = "nuget-feed-enabled";
  private static final Logger LOG = Logger.getInstance(FeedServerSettingsController.class.getName());

  @NotNull private final NuGetServerJavaSettings mySettings;
  @NotNull private final NuGetPackagesIndexer myPackagesIndexer;

  public FeedServerSettingsController(@NotNull final AuthorizationInterceptor auth,
                                      @NotNull final PluginDescriptor pluginDescriptor,
                                      @NotNull final PermissionChecker checker,
                                      @NotNull final WebControllerManager web,
                                      @NotNull final NuGetServerJavaSettings settings,
                                      @NotNull final NuGetPackagesIndexer packagesIndexer) {
    mySettings = settings;
    myPackagesIndexer = packagesIndexer;

    final String path = pluginDescriptor.getPluginResourcesPath("feed/settings.html");

    auth.addPathBasedPermissionsChecker(path, new RequestPermissionsChecker() {
      public void checkPermissions(@NotNull AuthorityHolder authorityHolder,
                                   @NotNull HttpServletRequest request) throws AccessDeniedException {
        checker.assertAccess(authorityHolder);
      }
    });
    web.registerController(path, this);
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    final Boolean enabled = getServerStatus(request);
    if (enabled == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }
    mySettings.setNuGetJavaFeedEnabled(enabled);
    if(enabled){
      LOG.info("NuGet feed was enabled. Start re-indexing NuGet builds metadata.");
      myPackagesIndexer.reindexAll();
    } else{
      LOG.info("NuGet feed was disabled. Newly published .nupkg files will not be indexed while feed is disabled.");
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return null;
  }

  @Nullable
  private Boolean getServerStatus(@NotNull final HttpServletRequest request) {
    final String v = request.getParameter(NUGET_FEED_ENABLED_PARAM_NAME);
    if (StringUtil.isEmptyOrSpaces(v)) return false;
    try {
      return Boolean.valueOf(v);
    } catch (Exception e) {
      return null;
    }
  }
}
