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

package jetbrains.buildServer.nuget.feed.server.javaFeed;

import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author Yegor.Yarko
 *         Date: 16.11.2009
 */
public class RequestWrapper extends HttpServletRequestWrapper {
  private final String myMapping;

  public RequestWrapper(@NotNull final HttpServletRequest request,
                        @NotNull final String mappingPath) {
    super(request);
    myMapping = mappingPath;
  }

  @Override
  public String getRequestURI() {
    final String uri = super.getRequestURI();
    //Workaound for Jersey baseUri and requestUri computation.
    if (uri.endsWith(myMapping)) return uri + "/";
    return uri;
  }

  @Override
  public StringBuffer getRequestURL() {
    final StringBuffer buf = super.getRequestURL();
    if (buf.toString().endsWith(myMapping)) buf.append("/");
    return buf;
  }

  @Override
  public String getPathInfo() {
    final String uri = getRequestURI();
    int i = uri.indexOf(myMapping);
    if (i >= 0) {
      String s = uri.substring(myMapping.length() + i);
      while(s.startsWith("/")) s = s.substring(1);
      return "/" + s;
    }

    //fallback
    return super.getPathInfo();
  }

  @Override
  public String getServletPath() {
    String fullPath = WebUtil.getPathWithoutContext(this);
    final int i = fullPath.indexOf(myMapping);
    if (i >= 0) {
      return fullPath.substring(0, i + myMapping.length());
    }
    //fallback
    return super.getServletPath();
  }
}
