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

package jetbrains.buildServer.nuget.server.feed.server.dotNetFeed;

import jetbrains.buildServer.controllers.BaseController;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 12:10
 */
public class MetadataControllerBase extends BaseController {
  @NotNull
  private final NuGetServerRunnerTokens myTokens;
  @NotNull
  private final MetadataControllerHandler myHandler;

  public MetadataControllerBase(@NotNull final NuGetServerRunnerTokens tokens,
                                @NotNull final MetadataControllerHandler handler) {
    myTokens = tokens;
    myHandler = handler;
  }

  @Override
  public ModelAndView doHandle(@NotNull final HttpServletRequest request,
                               @NotNull final HttpServletResponse response) throws Exception {

    final String key = request.getHeader(myTokens.getAccessTokenHeaderName());
    if (!myTokens.getAccessToken().equals(key)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return null;
    }

    myHandler.processRequest(request, response);
    return null;
  }
}
