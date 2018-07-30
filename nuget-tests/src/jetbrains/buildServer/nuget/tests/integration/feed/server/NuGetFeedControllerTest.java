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
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.NuGetUtils;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedController;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedProvider;
import jetbrains.buildServer.nuget.feed.server.controllers.requests.RecentNuGetRequests;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.http.HttpStatus;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.springframework.web.servlet.mvc.Controller;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Feed controller tests
 */
@Test
public class NuGetFeedControllerTest {

    private static final String SERVLET_PATH = "/app/nuget/v1/FeedService.svc";

    public void testWithHandler() throws Exception {
        Mockery m = new Mockery();
        WebControllerManager web = m.mock(WebControllerManager.class);
        NuGetServerSettings settings = m.mock(NuGetServerSettings.class);
        NuGetFeedProvider provider = m.mock(NuGetFeedProvider.class);
        NuGetFeedHandler handler = m.mock(NuGetFeedHandler.class);
        ProjectManager projectManager = m.mock(ProjectManager.class);
        RepositoryManager repositoryManager = m.mock(RepositoryManager.class);
        SProject project = m.mock(SProject.class);

        m.checking(new Expectations(){{
            allowing(settings).isNuGetServerEnabled(); will(returnValue(true));

            allowing(web).registerController(with(any(String.class)), with(any(Controller.class)));

            allowing(projectManager).findProjectByExternalId(with("_Root"));
            will(returnValue(project));

            allowing(project).getProjectId();
            will(returnValue("_Root"));

            allowing(repositoryManager).hasRepository(with(project), with(any(String.class)), with(any(String.class)));
            will(returnValue(true));

            allowing(provider).getHandler(with(any(HttpServletRequest.class))); will(returnValue(handler));

            allowing(handler).handleRequest(
                    with(any(NuGetFeedData.class)),
                    with(any(HttpServletRequest.class)),
                    with(any(HttpServletResponse.class)));
        }});

        Controller controller = new NuGetFeedController(web, settings,
                new RecentNuGetRequests(), provider, projectManager, repositoryManager);
        RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/Packages");
        ResponseWrapper response = new ResponseWrapper(new MockResponse());

        controller.handleRequest(request, response);

        m.assertIsSatisfied();
    }

    public void testWithoutHandler() throws Exception {
        Mockery m = new Mockery();
        WebControllerManager web = m.mock(WebControllerManager.class);
        NuGetServerSettings settings = m.mock(NuGetServerSettings.class);
        NuGetFeedProvider provider = m.mock(NuGetFeedProvider.class);
        ProjectManager projectManager = m.mock(ProjectManager.class);
        RepositoryManager repositoryManager = m.mock(RepositoryManager.class);
        SProject project = m.mock(SProject.class);

        m.checking(new Expectations(){{
            allowing(settings).isNuGetServerEnabled();
            will(returnValue(true));

            allowing(web).registerController(with(any(String.class)), with(any(Controller.class)));

            allowing(projectManager).findProjectByExternalId(with("_Root"));
            will(returnValue(project));

            allowing(project).getProjectId();
            will(returnValue("_Root"));

            allowing(repositoryManager).hasRepository(with(project), with(any(String.class)), with(any(String.class)));
            will(returnValue(true));

            allowing(provider).getHandler(with(any(HttpServletRequest.class)));
            will(returnValue(null));
        }});

        Controller controller = new NuGetFeedController(web, settings,
                new RecentNuGetRequests(), provider, projectManager, repositoryManager);
        RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/Packages");
        ResponseWrapper response = new ResponseWrapper(new MockResponse());

        controller.handleRequest(request, response);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_METHOD_NOT_ALLOWED);

        m.assertIsSatisfied();
    }

    public void testNewPathWithHandler() throws Exception {
        Mockery m = new Mockery();
        WebControllerManager web = m.mock(WebControllerManager.class);
        NuGetServerSettings settings = m.mock(NuGetServerSettings.class);
        NuGetFeedProvider provider = m.mock(NuGetFeedProvider.class);
        NuGetFeedHandler handler = m.mock(NuGetFeedHandler.class);
        ProjectManager projectManager = m.mock(ProjectManager.class);
        RepositoryManager repositoryManager = m.mock(RepositoryManager.class);
        SProject project = m.mock(SProject.class);

        m.checking(new Expectations(){{
            allowing(settings).isNuGetServerEnabled(); will(returnValue(true));

            allowing(web).registerController(with(any(String.class)), with(any(Controller.class)));

            allowing(projectManager).findProjectByExternalId(with(NuGetFeedData.DEFAULT.getProjectId()));
            will(returnValue(project));

            allowing(project).getProjectId();
            will(returnValue("_Root"));

            allowing(repositoryManager).hasRepository(with(project), with(any(String.class)), with(any(String.class)));
            will(returnValue(true));

            allowing(provider).getHandler(with(any(HttpServletRequest.class))); will(returnValue(handler));

            allowing(handler).handleRequest(
                    with(any(NuGetFeedData.class)),
                    with(any(HttpServletRequest.class)),
                    with(any(HttpServletResponse.class)));
        }});

        Controller controller = new NuGetFeedController(web, settings,
                new RecentNuGetRequests(), provider, projectManager, repositoryManager);
        String feedPath = NuGetUtils.getProjectFeedPath(NuGetFeedData.DEFAULT.getProjectId(), NuGetFeedData.DEFAULT.getFeedId());
        RequestWrapper request = new RequestWrapper(feedPath, SERVLET_PATH + "/Packages");
        ResponseWrapper response = new ResponseWrapper(new MockResponse());

        controller.handleRequest(request, response);

        m.assertIsSatisfied();
    }
}
