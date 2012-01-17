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

package jetbrains.buildServer.nuget.server.feed.server.controllers.dotNet;

import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerTokens;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 24.10.11 19:04
 */
public class PingBackController implements MetadataControllerHandler {
  private final MetadataControllersPaths myDescriptor;
  private final NuGetServerRunnerTokens mySettings;


  public PingBackController(@NotNull final MetadataControllersPaths descriptor,
                            @NotNull final NuGetServerRunnerTokens settings) {
    myDescriptor = descriptor;
    mySettings = settings;
  }

  @NotNull
  public String getControllerPath() {
    return myDescriptor.getPingControllerPath();
  }

  public void processRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
    response.setCharacterEncoding("utf-8");
    response.setContentType("text/plain");

    final String accessToken = mySettings.getServerToken();
    response.setHeader(mySettings.getServerTokenHeaderName(), accessToken);

    final PrintWriter writer = response.getWriter();
    writer.write(accessToken);
    writer.close();
  }
}
