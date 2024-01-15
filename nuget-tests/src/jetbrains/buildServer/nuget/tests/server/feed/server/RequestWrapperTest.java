

package jetbrains.buildServer.nuget.tests.server.feed.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.controllers.requests.RequestWrapper;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 17.01.12 21:21
 */
public class RequestWrapperTest extends BaseTestCase {
  private Mockery m;
  private HttpServletRequest req;
  private HttpServletRequest wrap;
  private static final String PATH = NuGetServerSettings.PROJECT_PATH;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    req = m.mock(HttpServletRequest.class);
    wrap = new RequestWrapper(req, PATH);
  }

  @Test
  public void testAddSlashForEmptyPath_URI() {
    m.checking(new Expectations(){{
      allowing(req).getRequestURI(); will(returnValue("zzz/" + PATH));
    }});

    Assert.assertEquals(wrap.getRequestURI(), "zzz/" + PATH + "/");
  }

  @Test
  public void testAddSlashForEmptyPath_URI_2() {
    m.checking(new Expectations(){{
      allowing(req).getRequestURI(); will(returnValue("zzz/" + PATH + "/aaa"));
    }});

    Assert.assertEquals(wrap.getRequestURI(), "zzz/" + PATH + "/aaa");
  }

  @Test
  public void testAddSlashForEmptyPath_URL() {
    m.checking(new Expectations(){{
      allowing(req).getHeader(with(any(String.class))); will(returnValue(null));
      allowing(req).getRequestURI(); will(returnValue(PATH));
      allowing(req).getScheme(); will(returnValue("http"));
      allowing(req).getServerName(); will(returnValue("zzz"));
      allowing(req).getServerPort(); will(returnValue(-1));
    }});

    Assert.assertEquals(wrap.getRequestURL().toString(), "http://zzz" + PATH + "/");
  }

  @Test
  public void testAddSlashForEmptyPath_URL_2() {
    m.checking(new Expectations(){{
      allowing(req).getHeader(with(any(String.class))); will(returnValue(null));
      allowing(req).getRequestURI(); will(returnValue(PATH + "/bbb"));
      allowing(req).getScheme(); will(returnValue("http"));
      allowing(req).getServerName(); will(returnValue("zzz"));
      allowing(req).getServerPort(); will(returnValue(-1));
    }});

    Assert.assertEquals(wrap.getRequestURL().toString(), "http://zzz" + PATH + "/bbb");
  }

  @Test
  public void test_mapsServerPath() {
    m.checking(new Expectations(){{
      allowing(req).getContextPath(); will(returnValue("/bs"));
      allowing(req).getRequestURI(); will(returnValue("/bs" + PATH + "/qqq"));
    }});

    Assert.assertEquals(wrap.getServletPath(), PATH);
  }

  @Test
  public void test_mapsPathInfo() {
    m.checking(new Expectations(){{
      allowing(req).getContextPath(); will(returnValue("/bs"));
      allowing(req).getRequestURI(); will(returnValue("/bs" + PATH + "/qqq"));
    }});

    Assert.assertEquals(wrap.getPathInfo(), "/qqq");
  }
}
