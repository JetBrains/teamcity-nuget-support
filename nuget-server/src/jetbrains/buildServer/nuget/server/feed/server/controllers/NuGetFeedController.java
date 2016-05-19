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

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.server.feed.server.controllers.requests.RecentNuGetRequests;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 17.01.12 17:40
 */
public class NuGetFeedController extends BaseController {

  private final String myNuGetPath;
  private final NuGetServerSettings mySettings;
  private final RecentNuGetRequests myRequestsList;
  private final Collection<NuGetFeedHandler> myHandlers;

  public NuGetFeedController(@NotNull final WebControllerManager web,
                             @NotNull final NuGetServerSettings settings,
                             @NotNull final RecentNuGetRequests requestsList,
                             @NotNull final Collection<NuGetFeedHandler> handlers) {
    mySettings = settings;
    myRequestsList = requestsList;
    myHandlers = handlers;
    myNuGetPath = settings.getNuGetFeedControllerPath();

    web.registerController(myNuGetPath + "/**", this);
  }


  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    if (!mySettings.isNuGetServerEnabled()) {
      return NuGetResponseUtil.nugetFeedIsDisabled(response);
    }

    String requestPath = WebUtil.getPathWithoutAuthenticationType(request);
    if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;

    final String path = requestPath.substring(myNuGetPath.length());
    final String query = request.getQueryString();
    final String pathAndQuery = path + (query != null ? ("?" + query) : "");
    myRequestsList.reportFeedRequest(pathAndQuery);

    final long startTime = new Date().getTime();
    for (NuGetFeedHandler handler : myHandlers) {
      if (handler.isAvailable()) {
        handler.handleRequest(myNuGetPath, request, response);
        myRequestsList.reportFeedRequestFinished(pathAndQuery, new Date().getTime() - startTime);
        return null;
      }
    }
    myRequestsList.reportFeedRequestFinished(pathAndQuery, new Date().getTime() - startTime);
    return NuGetResponseUtil.noImplementationFoundError(response);
  }
}
