

package jetbrains.buildServer.nuget.feed.server.odata4j;

import jetbrains.buildServer.util.CollectionsUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

/**
 * Configuration for ODataService.
 */
public class ODataServletConfig implements ServletConfig {
  private final Map<String, String> myParameters;
  private final ServletContext minimalServletContext = new ODataMinimalServletContext();

  public ODataServletConfig() {
    myParameters = CollectionsUtil.asMap("com.sun.jersey.config.property.packages", "jetbrains.buildServer.nuget");
  }

  @Override
  public String getServletName() {
    return "NuGet Feed";
  }

  @Override
  public ServletContext getServletContext() {
    return minimalServletContext;
  }

  @Override
  public String getInitParameter(String name) {
    return myParameters.get(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return new Vector<>(myParameters.keySet()).elements();
  }
}
