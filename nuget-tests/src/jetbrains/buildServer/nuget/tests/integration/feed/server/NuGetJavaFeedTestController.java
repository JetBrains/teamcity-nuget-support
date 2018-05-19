/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.net.URI;

@Path("")
public class NuGetJavaFeedTestController {

  @GET
  @Path(NuGetJavaFeedIntegrationTestBase.SERVLET_PATH + "{query: .*}")
  @Consumes("*/*")
  public Response getCommandResponse(@Context UriInfo uriInfo, @PathParam("query") String query) throws Exception {
    return getResponse(getRequest(uriInfo));
  }

  @POST
  @Path(NuGetJavaFeedIntegrationTestBase.SERVLET_PATH + "{query: .*}")
  @Consumes("*/*")
  public Response processBatch(@Context UriInfo uriInfo,
                               @PathParam("query") String query,
                               @HeaderParam("Content-Type") String contentType,
                               byte[] body) throws Exception {
    RequestWrapper request = getRequest(uriInfo);
    request.setMethod(HttpMethod.POST);
    request.setContentType(contentType);
    request.setBody(body);

    return getResponse(request);
  }

  @GET
  @Path(NuGetJavaFeedIntegrationTestBase.DOWNLOAD_URL)
  @Consumes("*/*")
  public Response getDownloadResponse1() throws Exception {
    return getFileResponse();
  }

  @GET
  @Path("/app" + NuGetJavaFeedIntegrationTestBase.DOWNLOAD_URL)
  @Consumes("*/*")
  public Response getDownloadResponse2() throws Exception {
    return getFileResponse();
  }

  @NotNull
  private Response getFileResponse() {
    final File file = new File("testData/packages/WebActivator.1.4.4.nupkg");
    return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
      .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" )
      .header("Content-Length", String.valueOf(file.length()))
      .build();
  }

  @NotNull
  private RequestWrapper getRequest(@NotNull UriInfo uriInfo) {
    final URI uri = uriInfo.getRequestUri();
    final String query = StringUtil.isEmpty(uri.getRawQuery()) ? StringUtil.EMPTY : "?" + uri.getRawQuery();
    RequestWrapper request = new RequestWrapper(NuGetJavaFeedIntegrationTestBase.SERVLET_PATH, uri.getRawPath() + query);
    request.setServerPort(uri.getPort());
    return request;
  }

  @NotNull
  private Response getResponse(@NotNull HttpServletRequest request) throws Exception {
    final NuGetFeedHandler handler = NuGetJavaFeedControllerIoC.getFeedProvider().getHandler(request);
    final ResponseWrapper response = new ResponseWrapper(new MockResponse());

    handler.handleRequest(NuGetJavaFeedIntegrationTestBase.FEED_DATA, request, response);

    Response.ResponseBuilder responseBuilder = Response
            .status(response.getStatus())
            .entity(response.toString());

    for (String name : response.getHeaderNames()) {
      responseBuilder.header(name, response.getHeader(name));
    }

    return responseBuilder.build();
  }


}
