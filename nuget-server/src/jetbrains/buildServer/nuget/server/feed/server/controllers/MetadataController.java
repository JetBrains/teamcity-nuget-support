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

import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerTokens;
import jetbrains.buildServer.nuget.server.feed.server.PackagesIndex;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:05
 */
public class MetadataController extends MetadataControllerBase {
  @NotNull private final MetadataControllersPaths myDescriptor;
  @NotNull private final NuGetServerRunnerTokens mySettings;
  @NotNull private final PackagesWriter myWriter;

  public MetadataController(@NotNull final MetadataControllersPaths descriptor,
                            @NotNull final NuGetServerRunnerTokens settings,
                            @NotNull final PackagesWriter writer) {
    myDescriptor = descriptor;
    mySettings = settings;
    myWriter = writer;
  }

  @NotNull
  @Override
  protected String getControllerPath() {
    return myDescriptor.getMetadataControllerPath();
  }

  @Override
  public ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {

    final String key = request.getHeader(mySettings.getAccessTokenHeaderName());
    if (!mySettings.getAccessToken().equals(key)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return null;
    }

    myWriter.serializePackages(request, response);
    return null;
  }
}
