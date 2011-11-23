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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerUri;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.net.URI;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 16:17
 */
public class NuGetFeedProxyController extends BaseController {
  private static final Logger LOG = Logger.getInstance(NuGetFeedProxyController.class.getName());

  @NotNull private final FeedClient myClient;
  @NotNull private final NuGetServerRunnerSettings mySettings;
  @NotNull private final RecentNuGetRequests myRequestsList;
  @NotNull private final NuGetServerUri myUri;
  @NotNull private final String myNuGetPath;

  public NuGetFeedProxyController(@NotNull final WebControllerManager web,
                                  @NotNull final FeedClient client,
                                  @NotNull final NuGetServerUri uri,
                                  @NotNull final NuGetServerRunnerSettings settings,
                                  @NotNull final RecentNuGetRequests requestsList) {
    myUri = uri;
    myClient = client;
    mySettings = settings;
    myRequestsList = requestsList;
    myNuGetPath = settings.getNuGetFeedControllerPath();

    web.registerController(myNuGetPath + "/**", this);
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    LOG.debug("Feed request: " + WebUtil.createPathWithParameters(request));

    if (!mySettings.isNuGetFeedEnabled()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "NuGet Feed server is not switched on in server configuration");
    }

    String requestPath = WebUtil.getPathWithoutAuthenticationType(request);

    if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;

    if (!requestPath.startsWith(myNuGetPath) && !requestPath.equals(myNuGetPath)) {
      response.sendError(HttpStatus.SC_NOT_FOUND, "Path not found");
      return null;
    }

    final String baseFeed = myUri.getNuGetFeedBaseUri();
    if (baseFeed == null) {
      response.sendError(HttpStatus.SC_SERVICE_UNAVAILABLE);
      return null;
    }

    final String path = requestPath.substring(myNuGetPath.length());
    final String query = request.getQueryString();

    final HttpRequestBase method = createRequest(request);
    final String pathAndQuery = path + (query != null ? ("?" + query) : "");
    myRequestsList.reportFeedRequest(pathAndQuery);
    method.setURI(new URI(baseFeed + pathAndQuery));
    final String baseUrl = getFeedUrlBase(request);

    method.setHeader("X-TeamCityUrl", baseUrl);
    method.setHeader("X-TeamCityFeedBase", baseUrl + mySettings.getNuGetFeedControllerPath());

    try {
      final HttpResponse resp = myClient.execute(method);
      response.setStatus(resp.getStatusLine().getStatusCode());

      final HttpEntity entity = resp.getEntity();
      if (entity == null) return null;

      final Header encodingHeader = entity.getContentEncoding();
      if (encodingHeader != null) {
        response.setCharacterEncoding(encodingHeader.getValue());
      }
      final Header contentTypeHeader = entity.getContentType();
      if (contentTypeHeader != null) {
        response.setContentType(contentTypeHeader.getValue());
      }

      if (LOG.isDebugEnabled()) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        entity.writeTo(bos);
        LOG.debug("Returned from server: " + bos.toString("utf-8"));
        response.getOutputStream().write(bos.toByteArray());
      } else {
        entity.writeTo(response.getOutputStream());
      }

      return null;
    } catch (Throwable t) {
      LOG.warn("Failed to process request to NuGet server to " + method.getURI());
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "" + t.getMessage());
      return null;
    } finally {
      method.abort();
    }
  }

  /**
   * @param request request
   * @return path with httpAuth or guestAuth infix if any
   */
  @NotNull
  private String getFeedUrlBase(@NotNull final HttpServletRequest request) {
    String baseUrl = WebUtil.getPathWithoutContext(request);
    if (!baseUrl.startsWith("/")) baseUrl = "/" + baseUrl;

    int idx = baseUrl.indexOf(mySettings.getNuGetFeedControllerPath());
    String infix = "";
    if (idx > 0) {
      infix = baseUrl.substring(0, idx);
    }

    String rootUrl = WebUtil.getRootUrl(request);
    while(rootUrl.endsWith("/")) rootUrl = rootUrl.substring(0, rootUrl.length()-1);

    return rootUrl + infix;
  }

  @NotNull
  private HttpRequestBase createRequest(@NotNull final HttpServletRequest request) {
    final String method = request.getMethod();
    if (method.equalsIgnoreCase("get")) {
      return new HttpGet();
    }

    if (method.equalsIgnoreCase("head")) {
      return new HttpHead();
    }

    LOG.warn("Unsupported http method: " + method);
    return new HttpGet();
  }
}
