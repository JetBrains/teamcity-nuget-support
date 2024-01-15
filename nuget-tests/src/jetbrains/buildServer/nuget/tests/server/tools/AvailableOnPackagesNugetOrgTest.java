

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedClient;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.tool.impl.AvailableOnPackagesNugetOrg;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.tools.available.FetchAvailableToolsResult;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

/**
 * @author Evgeniy.Koshkin
 */
public class AvailableOnPackagesNugetOrgTest extends BaseTestCase {

  private Mockery m;
  private NuGetFeedClient myClient;
  private NuGetFeedReader myReader;
  private AvailableOnPackagesNugetOrg myFetcher;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    myClient = m.mock(NuGetFeedClient.class);
    myReader = m.mock(NuGetFeedReader.class);

    myFetcher = new AvailableOnPackagesNugetOrg(myClient, myReader);

    m.checking(new Expectations(){{
      allowing(myClient).withCredentials(null); will(returnValue(myClient));
    }});
  }

  @Test
  public void test_should_try_both_feeds_on_error() throws IOException {

    m.checking(new Expectations(){{
      oneOf(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v1/FeedService.svc", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      oneOf(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
    }});

    final FetchAvailableToolsResult result = myFetcher.fetchAvailable();
    if(result.getErrors().isEmpty())
      fail();

    m.assertIsSatisfied();
  }

  @Test
  public void test_should_work_on_one_feed_error_1() throws IOException {

    m.checking(new Expectations(){{
      allowing(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
      oneOf(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v1/FeedService.svc", "NuGet.CommandLine"); will(returnValue(Collections.emptyList()));
    }});

    myFetcher.fetchAvailable();
    m.assertIsSatisfied();
  }

  @Test
  public void test_should_work_on_one_feed_error_2() throws IOException {

    m.checking(new Expectations(){{
      oneOf(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v2", "NuGet.CommandLine"); will(returnValue(Collections.emptyList()));
      allowing(myReader).queryPackageVersions(myClient, "http://packages.nuget.org/api/v1/FeedService.svc", "NuGet.CommandLine"); will(throwException(new IOException("oops")));
    }});

    myFetcher.fetchAvailable();
    m.assertIsSatisfied();
  }
}
