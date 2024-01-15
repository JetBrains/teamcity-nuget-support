

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.controllers.MockResponse;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.NuGetUtils;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedController;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler;
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedProvider;
import jetbrains.buildServer.nuget.feed.server.controllers.requests.RecentNuGetRequests;
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.NuGetServiceFeedHandler;
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.NuGetServiceFeedHandlerContext;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.http.HttpStatus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.web.servlet.mvc.Controller;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Feed controller tests
 */
@Test
public class NuGetFeedControllerTest {

    private static final String SERVLET_PATH = "/app/nuget/v1/FeedService.svc";
    private static final String SERVICE_FEED_BASE_PATH = "/app/nuget/feed/publishPackageServiceFeed";

    public void testWithHandler() throws Exception {
        Mockery m = new Mockery();
        WebControllerManager web = m.mock(WebControllerManager.class);
        NuGetServerSettings settings = m.mock(NuGetServerSettings.class);
        NuGetFeedProvider provider = m.mock(NuGetFeedProvider.class);
        NuGetFeedHandler handler = m.mock(NuGetFeedHandler.class);
        ProjectManager projectManager = m.mock(ProjectManager.class);
        RepositoryManager repositoryManager = m.mock(RepositoryManager.class);
        NuGetServiceFeedHandler serviceFeedHandler = m.mock(NuGetServiceFeedHandler.class);
        SProject project = m.mock(SProject.class);

        m.checking(new Expectations(){{
            allowing(settings).isNuGetServerEnabled(); will(returnValue(true));

            allowing(web).registerController(with(any(String.class)), with(any(Controller.class)));

            allowing(projectManager).findProjectByExternalId(with("_Root"));
            will(returnValue(project));

            allowing(project).getProjectId();
            will(returnValue("_Root"));
            allowing(project).getExternalId();
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
                new RecentNuGetRequests(), provider, projectManager, repositoryManager, serviceFeedHandler);
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
        NuGetServiceFeedHandler serviceFeedHandler = m.mock(NuGetServiceFeedHandler.class);
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
                new RecentNuGetRequests(), provider, projectManager, repositoryManager, serviceFeedHandler);
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
        NuGetServiceFeedHandler serviceFeedHandler = m.mock(NuGetServiceFeedHandler.class);
        SProject project = m.mock(SProject.class);

        m.checking(new Expectations(){{
            allowing(settings).isNuGetServerEnabled(); will(returnValue(true));

            allowing(web).registerController(with(any(String.class)), with(any(Controller.class)));

            allowing(projectManager).findProjectByExternalId(with(NuGetFeedData.DEFAULT.getProjectId()));
            will(returnValue(project));

            allowing(project).getProjectId();
            will(returnValue("_Root"));
            allowing(project).getExternalId();
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
                new RecentNuGetRequests(), provider, projectManager, repositoryManager, serviceFeedHandler);
        String feedPath = NuGetUtils.getProjectFeedPath(NuGetFeedData.DEFAULT.getProjectId(), NuGetFeedData.DEFAULT.getFeedId(), NuGetAPIVersion.V2);
        RequestWrapper request = new RequestWrapper(feedPath, SERVLET_PATH + "/Packages");
        ResponseWrapper response = new ResponseWrapper(new MockResponse());

        controller.handleRequest(request, response);

        m.assertIsSatisfied();
    }

    public void shouldServiceFeedHandleIncorrectProjectId() throws Exception {
      Mockery m = new Mockery();
      WebControllerManager web = m.mock(WebControllerManager.class);
      NuGetServerSettings settings = m.mock(NuGetServerSettings.class);
      NuGetFeedProvider provider = m.mock(NuGetFeedProvider.class);
      ProjectManager projectManager = m.mock(ProjectManager.class);
      RepositoryManager repositoryManager = m.mock(RepositoryManager.class);
      NuGetServiceFeedHandler serviceFeedHandler = m.mock(NuGetServiceFeedHandler.class);

      m.checking(new Expectations(){{
        oneOf(settings).isNuGetServerEnabled(); will(returnValue(true));

        exactly(2).of(web).registerController(with(any(String.class)), with(any(Controller.class)));

        oneOf(projectManager).findProjectByExternalId(with("Invalid"));
        will(returnValue(null));
      }});

      Controller controller = new NuGetFeedController(web, settings,
                                                      new RecentNuGetRequests(), provider, projectManager, repositoryManager, serviceFeedHandler);
      RequestWrapper request = new RequestWrapper(SERVICE_FEED_BASE_PATH, SERVICE_FEED_BASE_PATH + "/Invalid/");
      ResponseWrapper response = new ResponseWrapper(new MockResponse());

      controller.handleRequest(request, response);

      m.assertIsSatisfied();
      Assert.assertEquals(response.getStatus(), 404);
    }

  public void shouldServiceFeedHandleRequest() throws Exception {
    Mockery m = new Mockery();
    WebControllerManager web = m.mock(WebControllerManager.class);
    NuGetServerSettings settings = m.mock(NuGetServerSettings.class);
    NuGetFeedProvider provider = m.mock(NuGetFeedProvider.class);
    ProjectManager projectManager = m.mock(ProjectManager.class);
    RepositoryManager repositoryManager = m.mock(RepositoryManager.class);
    NuGetServiceFeedHandler serviceFeedHandler = m.mock(NuGetServiceFeedHandler.class);
    SProject project = m.mock(SProject.class);
    String projectId = "ProjectId";
    RequestWrapper request = new RequestWrapper(SERVICE_FEED_BASE_PATH, SERVICE_FEED_BASE_PATH + "/" + projectId + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    m.checking(new Expectations(){{
      oneOf(settings).isNuGetServerEnabled(); will(returnValue(true));

      exactly(2).of(web).registerController(with(any(String.class)), with(any(Controller.class)));

      oneOf(projectManager).findProjectByExternalId(with(projectId));
      will(returnValue(project));

      oneOf(serviceFeedHandler).handleRequest(with(createContextMatcher(projectId)),
                                              with(any(HttpServletRequest.class)),
                                              with(equal(response)));
    }});

    Controller controller = new NuGetFeedController(web, settings,
                                                    new RecentNuGetRequests(), provider, projectManager, repositoryManager, serviceFeedHandler);

    controller.handleRequest(request, response);

    m.assertIsSatisfied();
  }

  private Matcher<NuGetServiceFeedHandlerContext> createContextMatcher(String projectId) {
    return new BaseMatcher<NuGetServiceFeedHandlerContext>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("NuGetServiceFeedHandlerContext.ProjectId should mathch:").appendValue(projectId);
      }

      @Override
      public boolean matches(Object o) {
        if (!(o instanceof NuGetServiceFeedHandlerContext)) return false;
        NuGetServiceFeedHandlerContext value = (NuGetServiceFeedHandlerContext)o;
        return value.getProjectId().equals(projectId);
      }
    };
  }
}
