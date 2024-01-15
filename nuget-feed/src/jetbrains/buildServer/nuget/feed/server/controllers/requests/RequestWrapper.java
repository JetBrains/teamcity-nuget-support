

package jetbrains.buildServer.nuget.feed.server.controllers.requests;

import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.UriBuilder;

/**
 * @author Yegor.Yarko
 *         Date: 16.11.2009
 */
public class RequestWrapper extends HttpServletRequestWrapper {
  private static final String FORWARD_SLASH = "/";
  private final HttpServletRequest myRequest;
  private final String myMapping;

  public RequestWrapper(@NotNull final HttpServletRequest request,
                        @NotNull final String mappingPath) {
    super(request);
    myRequest = request;
    myMapping = mappingPath;
  }

  @Override
  public String getRequestURI() {
    final String uri = super.getRequestURI();
    //Workaround for Jersey baseUri and requestUri computation.
    if (uri.endsWith(myMapping)) return uri + FORWARD_SLASH;
    return uri;
  }

  @Override
  public StringBuffer getRequestURL() {
    final UriBuilder uriBuilder = UriBuilder.fromPath(getRequestURI())
      .scheme(getScheme())
      .host(getServerName())
      .port(getServerPort());

    return new StringBuffer(uriBuilder.build().toString());
  }

  @Override
  public String getPathInfo() {
    final String uri = getRequestURI();
    int i = uri.indexOf(myMapping);
    if (i >= 0) {
      String s = uri.substring(myMapping.length() + i);
      while (s.startsWith(FORWARD_SLASH)) s = s.substring(1);
      return FORWARD_SLASH + s;
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

  @Override
  public String getScheme() {
    return WebUtil.getScheme(myRequest);
  }

  @Override
  public String getServerName() {
    return WebUtil.getServerName(myRequest);
  }

  @Override
  public int getServerPort() {
    final int serverPort = WebUtil.getServerPort(myRequest);
    final int defaultPort = WebUtil.getDefaultPort(getScheme());
    return serverPort == defaultPort ? -1 : serverPort;
  }

  @Override
  public String getParameter(String name) {
    final String value = super.getParameter(name);
    if (value == null) return null;
    // NuGet client appends forward clash at the end of query string
    return StringUtil.trimEnd(value, FORWARD_SLASH);
  }
}
