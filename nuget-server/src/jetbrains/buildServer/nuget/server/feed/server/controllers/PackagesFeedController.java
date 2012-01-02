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

import jetbrains.buildServer.controllers.BaseController;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:49
 */
public class PackagesFeedController extends BaseController {
  private final NuGetProducer myProducer;

  public PackagesFeedController(@NotNull final NuGetProducer producer) {
    myProducer = producer;
  }

  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request,
                                  @NotNull HttpServletResponse response) throws Exception {
    if (!isGet(request)) {
      //error response according to OData spec for unsupported oprtaions (modification operations)
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
    }

    return null;
  }
}
