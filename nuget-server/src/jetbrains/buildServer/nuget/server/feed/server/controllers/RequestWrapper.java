/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author Yegor.Yarko
 *         Date: 16.11.2009
 */
public class RequestWrapper extends HttpServletRequestWrapper {
  private final String myExtraServletPath;

  public RequestWrapper(@NotNull final HttpServletRequest request,
                        @NotNull final String extraServletPath) {
    super(request);
    myExtraServletPath = extraServletPath;
  }

  @Override
  public String getPathInfo() {
    final String info = super.getPathInfo();
    if (info.startsWith(myExtraServletPath)) {
      return info.substring(myExtraServletPath.length());
    }
    return info;
  }

  @Override
  public String getServletPath() {
    return super.getServletPath() + myExtraServletPath;
  }
}
