

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.tool.NuGetServerToolPreProcessor;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.tools.installed.ToolPaths;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by Evgeniy.Koshkin.
 */
public class NuGetServerToolPreProcessorTest extends BaseTestCase {

  private Mockery m;
  private NuGetServerToolPreProcessor myToolPreProcessor;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    final ToolPaths toolPaths = m.mock(ToolPaths.class);
    final ServerResponsibility serverResponsibility = m.mock(ServerResponsibility.class);
    m.checking(new Expectations(){{
      allowing(serverResponsibility).canManageServerConfiguration(); will(returnValue(true));
    }});
    myToolPreProcessor = new NuGetServerToolPreProcessor(new ServerPaths(createTempDir()), toolPaths, null, null, null, serverResponsibility);
  }

  @Test
  public void testNoTools() throws Exception {
    myToolPreProcessor.doBeforeServerStartup();
    myToolPreProcessor.doAfterServerStartup();
  }

  @Test
  public void testSimple() throws Exception {
    fail();
  }
}
