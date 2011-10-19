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
import java.net.URI;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 16:17
 */
public class NuGetFeedProxyController extends BaseController {
  private static final Logger LOG = Logger.getInstance(NuGetFeedProxyController.class.getName());
  public static final String NUGET_PATH = "/app/nuget";

  private final FeedClient myClient;
  private final NuGetServerUri myUri;

  public NuGetFeedProxyController(@NotNull final WebControllerManager web,
                                  @NotNull final FeedClient client,
                                  @NotNull final NuGetServerUri uri) {
    myUri = uri;
    myClient = client;

    web.registerController(NUGET_PATH + "/**", this);
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    String requestPath = WebUtil.getPathWithoutAuthenticationType(request);

    if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;

    if (!requestPath.startsWith(NUGET_PATH) && !requestPath.equals(NUGET_PATH)) {
      response.sendError(HttpStatus.SC_NOT_FOUND, "Path not found");
      return null;
    }

    final String baseFeed = myUri.getNuGetFeedBaseUri();
    if (baseFeed == null) {
      response.sendError(HttpStatus.SC_SERVICE_UNAVAILABLE);
      return null;
    }

    final String path = requestPath.substring(NUGET_PATH.length());
    final String query = request.getQueryString();

    final HttpRequestBase method = createRequest(request);
    method.setURI(new URI(baseFeed + path + (query != null ? ("?" + query) : "")));
    method.setHeader("X-TeamCityUrl", WebUtil.getRootUrl(request));

    final HttpResponse resp = myClient.execute(method);
    try {
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

      entity.writeTo(response.getOutputStream());

      return null;
    } finally {
      method.abort();
    }
  }

  @NotNull
  private HttpRequestBase createRequest(HttpServletRequest request) {
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
