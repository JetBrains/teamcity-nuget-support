/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.olingo;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCache;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler;
import jetbrains.buildServer.nuget.feed.server.olingo.processor.NuGetServiceFactory;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.olingo.odata2.core.servlet.ODataServlet;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Request handler based on Olingo library.
 */
public class OlingoRequestHandler implements NuGetFeedHandler {

  private static final Logger LOG = Logger.getInstance(OlingoRequestHandler.class.getName());
  private final NuGetServiceFactory myServiceFactory;
  private final ResponseCache myCache;

  public OlingoRequestHandler(@NotNull final NuGetServiceFactory serviceFactory,
                              @NotNull final ResponseCache cache) {
    myServiceFactory = serviceFactory;
    myCache = cache;
  }

  @Override
  public void handleRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
    if (TeamCityProperties.getBoolean("teamcity.nuget.feed.use.cache")) {
      myCache.getOrCompute(request, response, this::processFeedRequest);
    } else {
      processFeedRequest(request, response);
    }
  }

  private void processFeedRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    LOG.debug("NuGet Feed: " + WebUtil.getRequestDump(request) + "|" + request.getRequestURI());

    request.setAttribute("org.apache.olingo.odata2.service.factory.instance", myServiceFactory);

    ODataServlet servlet = new ODataServlet();
    servlet.init(new ODataServletConfig());

    try {
      servlet.service(request, response);
    } catch (Throwable e) {
      LOG.warnAndDebugDetails("Failed to process request", e);
      throw e;
    }
  }
}
