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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerJavaSettings;
import jetbrains.buildServer.nuget.server.feed.server.controllers.NuGetFeedHandler;
import org.jetbrains.annotations.NotNull;

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
public class ODataPackagesFeedController implements NuGetFeedHandler {
  private final ServletContainer myContainer;
  private final NuGetServerJavaSettings mySettings;

  public ODataPackagesFeedController(@NotNull final NuGetProducer producer,
                                     @NotNull final ServletConfig config,
                                     @NotNull final NuGetServerJavaSettings settings) {
    mySettings = settings;
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

  public boolean isAvailable() {
    return mySettings.isNuGetJavaFeedEnabled();
  }

  public void handleRequest(@NotNull final HttpServletRequest request,
                            @NotNull final HttpServletResponse response) throws Exception {

    if (!BaseController.isGet(request)) {
      //error response according to OData spec for unsupported oprtaions (modification operations)
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "TeamCity provided feed is readonly.");
    }

    myContainer.service(new RequestWrapper(request, "/app", "/nuget/v1/FeedService.svc"), response);
  }
}
