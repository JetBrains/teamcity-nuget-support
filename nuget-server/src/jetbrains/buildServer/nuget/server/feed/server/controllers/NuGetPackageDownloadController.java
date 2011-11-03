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

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.HttpDownloadProcessor;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 03.11.11 11:48
 */
public class NuGetPackageDownloadController extends BaseController {

  @NotNull
  private final MetadataControllersPaths myPaths;
  @NotNull private final BuildsManager myBuilds;
  @NotNull private final HttpDownloadProcessor myDownloadHandler;

  public NuGetPackageDownloadController(@NotNull final AuthorizationInterceptor auth,
                                        @NotNull final WebControllerManager web,
                                        @NotNull final MetadataControllersPaths paths,
                                        @NotNull final BuildsManager builds,
                                        @NotNull final HttpDownloadProcessor downloadHandler) {
    myPaths = paths;
    myBuilds = builds;
    myDownloadHandler = downloadHandler;
    final String path = paths.getArtifactsDownloadUrlBase() + "**";
    web.registerController(path, this);
    auth.addPathNotRequiringAuth(path);
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    final String prefixPath = myPaths.getArtifactsDownloadUrlWithTokenBase();

    String requestPath = WebUtil.getPathWithoutAuthenticationType(request);
    if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;

    if (!requestPath.startsWith(prefixPath)) {
      response.sendError(HttpStatus.SC_NOT_FOUND, "Path not found");
      return null;
    }

    final String buildAndPath = requestPath.substring(prefixPath.length());
    int slash = buildAndPath.indexOf('/');
    if (slash <= 0) {
      response.sendError(HttpStatus.SC_NOT_FOUND, "Path not found. Invalid path");
      return null;
    }
    final Long buildId = parseId(buildAndPath.substring(0, slash));
    final String path = buildAndPath.substring(slash + 1);

    if (buildId == null) {
      response.sendError(HttpStatus.SC_NOT_FOUND, "Path not found. Invalid buildId");
      return null;
    }

    final SBuild build = myBuilds.findBuildInstanceById(buildId);
    if (build == null) {
      response.sendError(HttpStatus.SC_NOT_FOUND, "Build not found.");
      return null;
    }

    final BuildArtifact artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getArtifact(path);
    if (artifact == null) {
      response.sendError(HttpStatus.SC_NOT_FOUND, "Build artifact not found.");
      return null;
    }

    return myDownloadHandler.processArtifactDownload(build, artifact, request, response);
  }

  @Nullable
  private Long parseId(@Nullable String id) {
    if (id == null || StringUtil.isEmptyOrSpaces(id)) {
      return null;
    }

    try {
      return Long.parseLong(id.trim());
    } catch(Exception e) {
      return null;
    }
  }
}
