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
import org.jetbrains.annotations.NotNull;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.resources.AbstractODataApplication;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:49
 */
public class PackagesFeedController extends BaseController {
  private final NuGetProducer myProducer;
  private final ServletContainer myContainer;
  private final String PATH = "/app/nuget2";
  private final RequestPathTransformInfo myPathTransformInfo;

  public PackagesFeedController(@NotNull final NuGetProducer producer,
                                @NotNull final ServletConfig config,
                                @NotNull final WebControllerManager web) {
    myProducer = producer;

    web.registerController(PATH + "/**", this);

    myContainer = new ServletContainer(new AbstractODataApplication(){
      @Override
      public Set<Object> getSingletons() {
        final Set<Object> set = new HashSet<Object>(super.getSingletons());
        set.add(new DefaultODataProducerProvider(){
          @Override
          protected ODataProducer createInstanceFromFactoryInContainerSpecificSetting() {
            return myProducer.getProducer();
          }
        });
        return set;
      }
    });

    try {
      myContainer.init(createServletConfig(config));
    } catch (ServletException e) {
      e.printStackTrace();
    }
    myPathTransformInfo = new RequestPathTransformInfo();
    myPathTransformInfo.setPathMapping(Collections.singletonMap(PATH, ""));
  }

  @NotNull
  private ServletContext createServletContext(@NotNull final ServletContext baseContext) {
    return (ServletContext) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{ServletContext.class},
            new InvocationHandler() {
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getContextPath")) {
                  return baseContext.getContextPath() + PATH;
                }
                return method.invoke(baseContext, args);
              }
            }
    );
  }
  
  private ServletConfig createServletConfig(@NotNull final ServletConfig config) {
    final Map<String, String> myInit = new HashMap<String, String>();
    final Enumeration<String> it = config.getInitParameterNames();
    while (it.hasMoreElements()) {
      final String key = it.nextElement();
      myInit.put(key, config.getInitParameter(key));
    }
    myInit.put("com.sun.jersey.config.property.packages", "jetbrains.buildServer.nuget");
    final ServletContext context = createServletContext(config.getServletContext());
    return new ServletConfig() {
      public String getServletName() {
        return config.getServletName();
      }

      public ServletContext getServletContext() {
        return context;
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

    myContainer.service(new RequestWrapper(request, myPathTransformInfo), response);

    return null;
  }
}
