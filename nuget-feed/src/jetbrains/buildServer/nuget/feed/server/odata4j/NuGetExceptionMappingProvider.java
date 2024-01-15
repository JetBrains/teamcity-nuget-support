

package jetbrains.buildServer.nuget.feed.server.odata4j;

import org.odata4j.exceptions.*;
import org.odata4j.producer.resources.ExceptionMappingProvider;

import javax.ws.rs.core.Response;
import java.util.regex.Pattern;

/**
 * Converts application exceptions into OData4j exceptions.
 */
public class NuGetExceptionMappingProvider extends ExceptionMappingProvider {

    private static final Pattern INVALID_KEY_PATTERN = Pattern.compile("for\\skey\\sId='[^']+',Version='");

    @Override
    public Response toResponse(RuntimeException e) {
        if (!(e instanceof ODataProducerException)) {
            // Return bad request exception when request handler not found
            e = new BadRequestException(e.getMessage());
        } else if (e instanceof NotFoundException) {
            // Package must have composite key (Id, Version)
            if (!INVALID_KEY_PATTERN.matcher(e.getMessage()).find()) {
                e = new BadRequestException(e.getMessage());
            }
        }

        return super.toResponse(e);
    }
}
