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

import jetbrains.buildServer.nuget.feed.server.controllers.*;
import jetbrains.buildServer.nuget.feed.server.json.JsonRequestHandler;
import jetbrains.buildServer.nuget.feed.server.odata4j.ODataRequestHandler;
import jetbrains.buildServer.nuget.feed.server.olingo.OlingoRequestHandler;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;

/**
 * @author Dmitry.Tretyakov
 *         Date: 19.12.2016
 *         Time: 14:34
 */
@Test
public class NuGetFeedProviderTests {

    private static final String SERVLET_PATH = "/app/nuget/v1/FeedService.svc";
    private NuGetFeedProviderImpl myFeedProvider;

    @BeforeMethod
    protected void setUp() throws Exception {
        final Mockery m = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        final ODataRequestHandler oDataRequestHandler = m.mock(ODataRequestHandler.class);
        final OlingoRequestHandler olingoRequestHandler = m.mock(OlingoRequestHandler.class);
        final JsonRequestHandler jsonRequestHandler = m.mock(JsonRequestHandler.class);
        final PackageUploadHandler uploadHandler = m.mock(PackageUploadHandler.class);
        myFeedProvider = new NuGetFeedProviderImpl(oDataRequestHandler, olingoRequestHandler, jsonRequestHandler, uploadHandler);
    }

    public void testGetPackagesHandler() {
        RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/Packages");

        NuGetFeedHandler handler = myFeedProvider.getHandler(request);

        Assert.assertNotNull(handler);
        Assert.assertTrue(handler instanceof OlingoRequestHandler);
    }

    public void testPushPackage() throws Exception {
        RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
        request.setMethod(HttpMethod.PUT);

        NuGetFeedHandler handler = myFeedProvider.getHandler(request);

        Assert.assertNotNull(handler);
        Assert.assertTrue(handler instanceof PackageUploadHandler);
    }

    public void testBatchRequest() throws Exception {
        RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/$batch");
        request.setMethod(HttpMethod.POST);

        NuGetFeedHandler handler = myFeedProvider.getHandler(request);

        Assert.assertNotNull(handler);
        Assert.assertTrue(handler instanceof OlingoRequestHandler);
    }

    public void testPostPackage() throws Exception {
        RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/Packages");
        request.setMethod(HttpMethod.POST);

        NuGetFeedHandler handler = myFeedProvider.getHandler(request);

        Assert.assertNull(handler);
    }

    public void testUpdatePackage() throws Exception {
        RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/Packages(Id='id',Version='1.0.0')");
        request.setMethod(HttpMethod.PUT);

        NuGetFeedHandler handler = myFeedProvider.getHandler(request);

        Assert.assertNull(handler);
    }

    public void testDeletePackage() throws Exception {
        RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/Packages(Id='id',Version='1.0.0')");
        request.setMethod(HttpMethod.DELETE);

        NuGetFeedHandler handler = myFeedProvider.getHandler(request);

        Assert.assertNull(handler);
    }
}
