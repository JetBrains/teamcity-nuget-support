package jetbrains.buildServer.nuget.feed.server.odata4j;

import org.odata4j.exceptions.BadRequestException;
import org.odata4j.producer.resources.ExceptionMappingProvider;

import javax.ws.rs.core.Response;

/**
 * Converts application exceptions into OData4j exceptions.
 */
public class NuGetExceptionMappingProvider extends ExceptionMappingProvider {

    @Override
    public Response toResponse(RuntimeException e) {
        if (e instanceof com.sun.jersey.api.NotFoundException) {
            // Return bad request exception when request handler not found
            e = new BadRequestException(e.getMessage());
        }

        return super.toResponse(e);
    }
}
