package jetbrains.buildServer.nuget.tests.integration.feed;

import jetbrains.buildServer.nuget.server.feed.FeedServer;
import jetbrains.buildServer.nuget.server.feed.render.FeedMetadataRenderer;
import jetbrains.buildServer.nuget.server.feed.render.NuGetContext;
import jetbrains.buildServer.nuget.server.feed.render.NuGetFeedRenderer;
import jetbrains.buildServer.nuget.server.feed.render.NuGetPackagesFeedRenderer;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.09.11 22:44
 */
public class NuGetFeedIntegrationTestBase extends IntegrationTestBase {
  private NuGetContext myContext;
  private FeedServer myFeed;
  private FeedHttpServer myServer;
  protected String myFeedUrl;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myContext = new NuGetContext();
    myFeed = new FeedServer(myContext, new NuGetFeedRenderer(), new FeedMetadataRenderer(), new NuGetPackagesFeedRenderer());
    final String baseUrl = "/feed";
    myServer = new FeedHttpServer(baseUrl, myFeed);
    myServer.start();
    myFeedUrl = "http://localhost:" + myServer.getPort() + baseUrl;
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    if (myServer != null) {
      myServer.stop();
    }
  }
}
