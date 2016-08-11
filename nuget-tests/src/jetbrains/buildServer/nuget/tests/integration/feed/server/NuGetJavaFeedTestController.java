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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.controllers.MockResponse;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler;
import jetbrains.buildServer.util.StringUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("")
public class NuGetJavaFeedTestController {

  @GET
  @Path(NuGetJavaFeedIntegrationTestBase.SERVLET_PATH + "{query: .*}")
  @Consumes("*/*")
  public Response getCommandResponse(@Context UriInfo uriInfo, @PathParam("query") String userName) throws Exception {
    final NuGetFeedHandler handler = NuGetJavaFeedControllerIoC.getFeedProvider().getHandler();

    // Request
    final URI uri = uriInfo.getRequestUri();
    final String path = uri.getRawPath() + (StringUtil.isEmpty(uri.getRawQuery()) ? StringUtil.EMPTY : "?" + uri.getRawQuery());
    final TestFeedRequestWrapper request = new TestFeedRequestWrapper("", path.substring(NuGetJavaFeedIntegrationTestBase.SERVLET_PATH.length()));

    // Response
    final MockResponse response = new MockResponse();
    final SerializableHttpServletResponseWrapper responseWrapper = new SerializableHttpServletResponseWrapper(response);

    handler.handleRequest(request, responseWrapper);

    return Response.status(Response.Status.OK).entity(responseWrapper.toString()).build();
  }
}