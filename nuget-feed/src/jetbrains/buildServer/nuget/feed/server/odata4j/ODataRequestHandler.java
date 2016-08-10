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

package jetbrains.buildServer.nuget.feed.server.odata4j;

import com.intellij.openapi.diagnostic.Logger;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCache;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.Util;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLWriterFactory2;
import org.odata4j.stax2.domimpl.DomXMLFactoryProvider2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:49
 */
public class ODataRequestHandler implements NuGetFeedHandler {
  private static final Logger LOG = Logger.getInstance(ODataRequestHandler.class.getName());

  private ServletContainer myContainer;
  private final ResponseCache myCache;

  public ODataRequestHandler(@NotNull final NuGetProducerHolder producer,
                             @NotNull final ResponseCache cache) {
    myCache = cache;
    try {
      myContainer = Util.doUnderContextClassLoader(getClass().getClassLoader(), () -> {
        ServletContainer sc = new ServletContainer(new NuGetODataApplication(producer));
        sc.init(new ODataServletConfig());
        return sc;
      });
    } catch (Throwable e) {
      LOG.warn("Failed to initialize NuGet Feed container. " + e.getMessage(), e);
    }
  }

  public void handleRequest(@NotNull final HttpServletRequest request,
                            @NotNull final HttpServletResponse response) throws Exception {
    if (myContainer == null) {
      //error response according to OData spec for unsupported oprtaions (modification operations)
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "TeamCity provided feed is not initialized.");
      return;
    }

    if (!BaseController.isGet(request)) {
      //error response according to OData spec for unsupported oprtaions (modification operations)
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "TeamCity provided feed is readonly.");
      return;
    }

    final ResponseCache.ComputeAction action = this::processFeedRequest;
    if (TeamCityProperties.getBoolean("teamcity.nuget.feed.use.cache")) {
      myCache.getOrCompute(request, response, action);
    } else {
      action.compute(request, response);
    }
  }

  private void processFeedRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    XMLFactoryProvider2.setInstance(DOM_XML_FACTORY_PROVIDER_2);
    LOG.debug("NuGet Feed: " + WebUtil.getRequestDump(request) + "|" + request.getRequestURI());

    Util.doUnderContextClassLoader(getClass().getClassLoader(), () -> {
      myContainer.service(request, response);
      return null;
    });
  }

  private static final DomXMLFactoryProvider2 DOM_XML_FACTORY_PROVIDER_2 = new DomXMLFactoryProvider2() {
    @Override
    public XMLWriterFactory2 newXMLWriterFactory2() {
      return ManualXMLWriter3::new;
    }
  };
}
