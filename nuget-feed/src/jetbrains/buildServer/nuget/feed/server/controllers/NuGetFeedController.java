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

package jetbrains.buildServer.nuget.feed.server.controllers;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.controllers.requests.RecentNuGetRequests;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Entry point for nuget feed.
 */
public class NuGetFeedController extends BaseController {

  private static final Pattern QUERY_ID = Pattern.compile("^(id=)(.*)", Pattern.CASE_INSENSITIVE);
  private final String myNuGetPath;
  private final NuGetFeedProvider myFeedProvider;
  private final NuGetServerSettings mySettings;
  private final RecentNuGetRequests myRequestsList;

  public NuGetFeedController(@NotNull final WebControllerManager web,
                             @NotNull final NuGetServerSettings settings,
                             @NotNull final RecentNuGetRequests requestsList,
                             @NotNull final NuGetFeedProvider feedProvider) {
    mySettings = settings;
    myRequestsList = requestsList;
    myNuGetPath = settings.getNuGetFeedControllerPath();
    myFeedProvider = feedProvider;

    web.registerController(myNuGetPath + "/**", this);
  }


  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    if (!mySettings.isNuGetServerEnabled()) {
      return NuGetResponseUtil.nugetFeedIsDisabled(response);
    }

    final HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
      @Override
      public String getQueryString() {
        final String queryString = super.getQueryString();

        if (super.getRequestURI().endsWith("FindPackagesById()")) {
          // NuGet client in VS 2015 Update 2 introduced breaking change where
          // instead of `id` parameter passed `Id` while OData is case sensitive
          return queryString != null ? QUERY_ID.matcher(queryString).replaceFirst("id=$2") : null;
        }

        return queryString;
      }
    };

    String requestPath = WebUtil.getPathWithoutAuthenticationType(requestWrapper);
    if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;

    final String path = requestPath.substring(myNuGetPath.length());
    final String query = requestWrapper.getQueryString();
    final String pathAndQuery = path + (query != null ? ("?" + query) : "");
    myRequestsList.reportFeedRequest(pathAndQuery);

    final NuGetFeedHandler feedHandler = myFeedProvider.getHandler();
    final long startTime = new Date().getTime();
    if (feedHandler.isAvailable()) {
      feedHandler.handleRequest(myNuGetPath, requestWrapper, response);
      myRequestsList.reportFeedRequestFinished(pathAndQuery, new Date().getTime() - startTime);
      return null;
    } else {
      myRequestsList.reportFeedRequestFinished(pathAndQuery, new Date().getTime() - startTime);
      return NuGetResponseUtil.noImplementationFoundError(response);
    }
  }
}
