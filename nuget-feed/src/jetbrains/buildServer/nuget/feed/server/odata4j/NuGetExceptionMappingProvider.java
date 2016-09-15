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
        return super.toResponse(new BadRequestException(e.getMessage()));
    }
}
