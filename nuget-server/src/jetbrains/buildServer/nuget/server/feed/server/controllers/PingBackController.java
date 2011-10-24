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
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 24.10.11 19:04
 */
public class PingBackController extends BaseController {
  private static final String PING_HEADER = "X-TeamCity-HostId";
  @NotNull
  private final String myPath;
  private final String myHash;

  public PingBackController(@NotNull final WebControllerManager web,
                            @NotNull final MetadataControllersPaths descriptor) {
    myHash = StringUtil.generateUniqueHash();

    myPath = descriptor.getPingControllerPath();
    web.registerController(myPath, this);
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  @NotNull
  public String getHash() {
    return myHash;
  }

  @NotNull
  public String getPingHeader() {
    return PING_HEADER;
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    response.setCharacterEncoding("utf-8");
    response.setContentType("text/plain");

    response.setHeader(PING_HEADER, myHash);
    final PrintWriter writer = response.getWriter();
    writer.write(myHash);
    writer.close();

    return null;
  }
}
