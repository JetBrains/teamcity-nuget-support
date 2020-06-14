/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import jetbrains.buildServer.web.util.WebUtil;

public class HttpServletRequestUtil {
  public static String getRootUrl(HttpServletRequest request) {
    final String rootUrl = WebUtil.getRootUrl(request);
    if (WebUtil.getServerPort(request) >= 0) {
      return rootUrl;
    }
    return UriBuilder.fromUri(rootUrl).host(WebUtil.getServerName(request)).build().toString();
  }

  public static String getRootUrlWithAuthenticationType(HttpServletRequest request) {
    final String rootUrl = getRootUrl(request);
    final String pathWithoutContext = WebUtil.getPathWithoutContext(request);
    final String pathWithoutAuthenticationType = WebUtil.getPathWithoutAuthenticationType(request);
    final int index = pathWithoutContext.indexOf(pathWithoutAuthenticationType);
    if (index > 0) {
      final String authenticationType = pathWithoutContext.substring(0, index);
      return rootUrl + authenticationType;
    }
    return rootUrl;
  }
}
