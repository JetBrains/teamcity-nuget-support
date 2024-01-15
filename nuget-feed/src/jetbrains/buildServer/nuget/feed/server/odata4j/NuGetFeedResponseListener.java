

package jetbrains.buildServer.nuget.feed.server.odata4j;

import com.intellij.openapi.diagnostic.Logger;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.monitoring.ResponseListener;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Evgeniy.Koshkin
 */
@Provider
public class NuGetFeedResponseListener implements ResponseListener {

  private final Logger LOG = Logger.getInstance(getClass().getName());

  public void onError(long id, Throwable ex) {
    LOG.warnAndDebugDetails("Error on processing NuGet feed response with ID " + id, ex);
  }

  public void onResponse(long id, ContainerResponse response) {
  }

  public void onMappedException(long id, Throwable exception, ExceptionMapper mapper) {
    LOG.warnAndDebugDetails("Exception on processing NuGet feed response with ID " + id, exception);
  }
}
