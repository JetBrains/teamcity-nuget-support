

package jetbrains.buildServer.nuget.feed.server.olingo.processor;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;

/**
 * Callback for handling errors by logging internal server errors additionally.
 * 
 */
public class NuGetErrorCallback implements ODataErrorCallback {

  private static final Logger LOG = Logger.getInstance(NuGetErrorCallback.class.getName());

  @Override
  public ODataResponse handleError(final ODataErrorContext context) throws ODataApplicationException {
    if (context.getHttpStatus() == HttpStatusCodes.INTERNAL_SERVER_ERROR) {
      LOG.warnAndDebugDetails("Internal Server Error", context.getException());
    }

    return EntityProvider.writeErrorDocument(context);
  }
}
