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
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.server.feed.server.controllers.NuGetFeedController;
import jetbrains.buildServer.nuget.server.feed.server.controllers.NuGetFeedHandler;
import jetbrains.buildServer.nuget.server.feed.server.controllers.requests.RecentNuGetRequests;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.http.HttpStatus;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.web.servlet.mvc.Controller;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.util.Collections;

/**
 * Feed controller tests
 */
public class NuGetFeedControllerTest extends NuGetJavaFeedIntegrationTestBase {

  private String SERVLET_PATH;
  private NuGetFeedController myController;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    Mockery m = new Mockery();
    WebControllerManager web = m.mock(WebControllerManager.class);
    NuGetServerSettings settings = m.mock(NuGetServerSettings.class);
    NuGetFeedHandler handler = m.mock(NuGetFeedHandler.class);

    m.checking(new Expectations(){{
      allowing(settings).getNuGetFeedControllerPath(); will(returnValue("/path"));
      allowing(settings).isNuGetServerEnabled(); will(returnValue(true));

      allowing(web).registerController(with(any(String.class)), with(any(Controller.class)));

      allowing(handler).handleRequest(with(any(String.class)), with(any(HttpServletRequest.class)), with(any(HttpServletResponse.class)));
      allowing(handler).isAvailable(); will(returnValue(true));
    }});

    myController = new NuGetFeedController(web, settings, new RecentNuGetRequests(), Collections.singletonList(handler));
    SERVLET_PATH = getNuGetServerUrl();
  }

  @Test
  public void testCreatePackage() throws Exception {
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "Packages");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());
    request.setMethod(HttpMethod.POST);

    myController.handleRequest(request, response);
    Assert.assertEquals(response.getStatus(), HttpStatus.SC_METHOD_NOT_ALLOWED);
  }

  @Test
  public void testUpdatePackage() throws Exception {
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "Packages(Id='id',Version='1.0.0')");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());
    request.setMethod(HttpMethod.PUT);

    myController.handleRequest(request, response);
    Assert.assertEquals(response.getStatus(), HttpStatus.SC_METHOD_NOT_ALLOWED);
  }

  @Test
  public void testDeletePackage() throws Exception {
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "Packages(Id='id',Version='1.0.0')");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());
    request.setMethod(HttpMethod.DELETE);

    myController.handleRequest(request, response);
    Assert.assertEquals(response.getStatus(), HttpStatus.SC_METHOD_NOT_ALLOWED);
  }

  @Test
  public void testGetPackages() throws Exception {
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "Packages");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    myController.handleRequest(request, response);
  }

  @Test
  public void testBatchRequest() throws Exception {
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "$batch");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());
    request.setMethod(HttpMethod.POST);

    myController.handleRequest(request, response);
  }
}
