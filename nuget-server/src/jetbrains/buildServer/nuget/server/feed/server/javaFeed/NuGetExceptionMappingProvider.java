/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed;

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
