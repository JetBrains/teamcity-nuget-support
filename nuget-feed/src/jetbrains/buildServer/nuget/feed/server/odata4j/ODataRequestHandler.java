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
import jetbrains.buildServer.nuget.feed.server.NuGetServerJavaSettings;
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCache;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.FuncThrow;
import jetbrains.buildServer.util.Util;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLWriter2;
import org.odata4j.stax2.XMLWriterFactory2;
import org.odata4j.stax2.domimpl.DomXMLFactoryProvider2;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:49
 */
public class ODataRequestHandler implements NuGetFeedHandler {
  private static final Logger LOG = Logger.getInstance(ODataRequestHandler.class.getName());

  private ServletContainer myContainer;
  private final NuGetServerJavaSettings mySettings;
  private final ResponseCache myCache;

  public ODataRequestHandler(@NotNull final NuGetProducerHolder producer,
                             @NotNull final ServletConfig config,
                             @NotNull final NuGetServerJavaSettings settings,
                             @NotNull final ResponseCache cache) {
    mySettings = settings;
    myCache = cache;
    try {
      myContainer = Util.doUnderContextClassLoader(getClass().getClassLoader(), new FuncThrow<ServletContainer, ServletException>() {
        public ServletContainer apply() throws ServletException {
          ServletContainer sc = new ServletContainer(new NuGetODataApplication(producer));
          sc.init(createServletConfig(config));
          return sc;
        }
      });
    } catch (Throwable e) {
      LOG.warn("Failed to initialize NuGet Feed container. " + e.getMessage(), e);
    }
  }

  private ServletConfig createServletConfig(@NotNull final ServletConfig config) {
    final Map<String, String> myInit = new HashMap<String, String>();
    final Enumeration<String> it = config.getInitParameterNames();
    while (it.hasMoreElements()) {
      final String key = it.nextElement();
      myInit.put(key, config.getInitParameter(key));
    }
    myInit.put("com.sun.jersey.config.property.packages", "jetbrains.buildServer.nuget");
    return new ServletConfig() {
      public String getServletName() {
        return config.getServletName();
      }

      public ServletContext getServletContext() {
        return config.getServletContext();
      }

      public String getInitParameter(String s) {
        return myInit.get(s);
      }

      public Enumeration<String> getInitParameterNames() {
        return new Vector<String>(myInit.keySet()).elements();
      }
    };
  }

  public boolean isAvailable() {
    return mySettings.isNuGetJavaFeedEnabled();
  }

  public void handleRequest(@NotNull final String baseMappingPath,
                            @NotNull final HttpServletRequest request,
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

    final ResponseCache.ComputeAction action = new ResponseCache.ComputeAction() {
      public void compute(@NotNull final HttpServletRequest request,
                          @NotNull final HttpServletResponse response) throws Exception {
        processFeedRequest(baseMappingPath, request, response);
      }
    };

    if (TeamCityProperties.getBoolean("teamcity.nuget.feed.use.cache")) {
      myCache.getOrCompute(request, response, action);
    } else {
      action.compute(request, response);
    }
  }

  private void processFeedRequest(final String baseMappingPath, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    XMLFactoryProvider2.setInstance(DOM_XML_FACTORY_PROVIDER_2);
    LOG.debug("NuGet Feed: " + WebUtil.getRequestDump(request) + "|" + request.getRequestURI());

    Util.doUnderContextClassLoader(getClass().getClassLoader(), new FuncThrow<Object, Exception>() {
      public Object apply() throws Exception {
        myContainer.service(new RequestWrapper(request, baseMappingPath), response);
        return null;
      }
    });
  }

  private static final DomXMLFactoryProvider2 DOM_XML_FACTORY_PROVIDER_2 = new DomXMLFactoryProvider2() {
    @Override
    public XMLWriterFactory2 newXMLWriterFactory2() {
      return new XMLWriterFactory2() {
        public XMLWriter2 createXMLWriter(Writer writer) {
          return new ManualXMLWriter3(writer);
        }
      };
    }
  };
}
