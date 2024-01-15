

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import com.sun.jersey.api.container.filter.LoggingFilter;
import org.odata4j.jersey.producer.server.ODataJerseyServer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.01.12 0:04
 */
public class ODataProducerUtil {
  public static ODataServer hostODataServer(String baseUri) {
    return startODataServer(baseUri);
  }

  public static ODataServer createODataServer(String baseUri) {
    return new ODataJerseyServer(baseUri, DefaultODataApplication.class, RootApplication.class)
            .addJerseyRequestFilter(LoggingFilter.class) // log all requests
            ;
  }

  public static ODataServer startODataServer(String baseUri) {
    return createODataServer(baseUri).start();
  }
}
