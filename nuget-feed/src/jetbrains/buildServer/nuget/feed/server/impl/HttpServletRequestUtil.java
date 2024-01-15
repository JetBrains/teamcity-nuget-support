

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
