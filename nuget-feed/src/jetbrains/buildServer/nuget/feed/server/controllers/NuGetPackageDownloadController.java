/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackageDownloadController extends BaseController {

  private static final Logger LOG = Logger.getInstance(NuGetPackageDownloadController.class.getName());

  @NotNull private final NuGetServerSettings myServerSettings;

  public NuGetPackageDownloadController(@NotNull final WebControllerManager web,
                                        @NotNull final NuGetServerSettings serverSettings) {
    myServerSettings = serverSettings;
    web.registerController(serverSettings.getNuGetFeedControllerPath() + "/download/**", this);
  }

  @Nullable
  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
    String requestPath = WebUtil.getPathWithoutAuthenticationType(request);
    if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
    final String feedControllerPath = myServerSettings.getNuGetFeedControllerPath();
    if(!requestPath.contains(feedControllerPath + "/download/")){
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    if (!myServerSettings.isNuGetServerEnabled()) {
      return NuGetResponseUtil.nugetFeedIsDisabled(response);
    }

    final String artifactDownloadUrl = requestPath.replace(feedControllerPath, "/repository");
    RequestDispatcher dispatcher = request.getRequestDispatcher(artifactDownloadUrl);
    if (dispatcher != null) {
      LOG.debug(String.format("Forwarding request. From %s To %s", requestPath, artifactDownloadUrl));
      dispatcher.forward(request, response);
    }
    return null;
  }
}
