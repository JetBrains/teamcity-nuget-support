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

import com.sun.jersey.spi.container.servlet.ServletContainer;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:49
 */
public class PackagesFeedController extends BaseController {
  private final ServletContainer myContainer;
  //TODO: move inside.
  //path after TeamCity Spring servlet mapping
  public static final String SERVLET_PATH = "/nuget2";
  //TODO: move inside
  public static final String PATH = "/app" + SERVLET_PATH;
  @NotNull
  private final RecentNuGetRequests myRequests;

  public PackagesFeedController(@NotNull final NuGetProducer producer,
                                @NotNull final ServletConfig config,
                                @NotNull final WebControllerManager web,
                                @NotNull final RecentNuGetRequests requests) {
    myRequests = requests;
    web.registerController(PATH + "/**", this);
    myContainer = new ServletContainer(new NuGetODataApplication(producer));

    try {
      myContainer.init(createServletConfig(config));
    } catch (ServletException e) {
      e.printStackTrace();
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

  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request,
                                  @NotNull HttpServletResponse response) throws Exception {
    if (!isGet(request)) {
      //error response according to OData spec for unsupported oprtaions (modification operations)
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
    }

    String requestPath = WebUtil.getPathWithoutAuthenticationType(request);
    if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;

    final String path = requestPath.substring(PATH.length());
    final String query = request.getQueryString();
    myRequests.reportFeedRequest(path + (query != null ? ("?" + query) : ""));

    myContainer.service(new RequestWrapper(request, SERVLET_PATH), response);
    return null;
  }

}
