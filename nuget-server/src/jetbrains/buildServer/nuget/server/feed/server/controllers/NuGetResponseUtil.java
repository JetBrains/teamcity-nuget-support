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

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetResponseUtil {
  private static final Logger LOG = Logger.getInstance(NuGetResponseUtil.class.getName());

  public static ModelAndView nugetFeedIsDisabled(@NotNull final HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "NuGet Feed server is not enabled in TeamCity server configuration");
    return null;
  }

  public static ModelAndView noImplementationFoundError(@NotNull final HttpServletResponse response) throws IOException {
    final String err = "No available " + NuGetFeedHandler.class + " implementations registered";
    LOG.warn(err);
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
    return null;
  }
}
