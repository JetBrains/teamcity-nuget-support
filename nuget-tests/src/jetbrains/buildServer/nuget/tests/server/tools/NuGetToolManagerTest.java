package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.impl.ToolPathsImpl;
import jetbrains.buildServer.nuget.server.settings.impl.NuGetSettingsManagerImpl;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.*;
import jetbrains.buildServer.serverSide.ServerPaths;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created 27.12.12 18:34
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetToolManagerTest extends BaseTestCase {
  private Mockery m;
  private AvailableToolsState myAvailables;
  private NuGetFeedReader myFeed;
  private ToolsWatcher myWatcher;
  private ToolPaths myPaths;
  private NuGetToolsInstaller myInstaller;
  private NuGetToolDownloader myDownloader;
  private ToolsRegistry myToolsRegistry;
  private NuGetToolManagerImpl myToolManager;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myAvailables = m.mock(AvailableToolsState.class);
    myFeed = m.mock(NuGetFeedReader.class);
    myWatcher = m.mock(ToolsWatcher.class);
    myInstaller = m.mock(NuGetToolsInstaller.class);
    myDownloader = m.mock(NuGetToolDownloader.class);
    myToolsRegistry = m.mock(ToolsRegistry.class);

    myPaths = new ToolPathsImpl(new ServerPaths(createTempDir().getPath()));
    myToolManager = new NuGetToolManagerImpl(
            myAvailables,
            myInstaller,
            myDownloader,
            myToolsRegistry,
            new NuGetToolsSettings(new NuGetSettingsManagerImpl())
            );
  }

  @Test
  public void test_default_tool_null() {
    m.checking(new Expectations(){{
      allowing(myToolsRegistry).getTools(); will(returnValue(Collections.emptyList()));
    }});
    Assert.assertNull(myToolManager.getDefaultTool());
  }

  @Test
  public void test_default_tool_2() {
    myToolManager.setDefaultTool("aaa");
    final NuGetInstalledTool it = m.mock(NuGetInstalledTool.class);
    m.checking(new Expectations(){{
      allowing(it).getId(); will(returnValue("aaa"));
      allowing(it).getPath(); will(returnValue(new File("some-path")));
      allowing(it).getVersion(); will(returnValue("42.333.667"));
      allowing(myToolsRegistry).getTools(); will(returnValue(Arrays.asList(it)));
    }});

    NuGetInstalledTool ret = myToolManager.getDefaultTool();
    Assert.assertNotNull(ret);
    Assert.assertTrue(ret.isDefaultTool());
    Assert.assertEquals(it.getId(), ret.getId());
    Assert.assertEquals(it.getPath(), ret.getPath());
    Assert.assertEquals(it.getVersion(), ret.getVersion());
  }

  @Test
  public void test_set_default() {
    myToolManager.setDefaultTool("some_tool");

    Assert.assertEquals("some_tool", myToolManager.getDefaultToolId());
  }

  @Test
  public void test_nuget_path_custom() throws IOException {
    File file = createTempFile();
    Assert.assertEquals(myToolManager.getNuGetPath(file.getPath()), file.getPath());
  }

  @Test
  public void test_nuget_path_preset() throws IOException {
    m.checking(new Expectations(){{
      allowing(myToolsRegistry).getNuGetPath("aaa"); will(returnValue(new File("some-path")));
    }});

    Assert.assertEquals(myToolManager.getNuGetPath("?aaa"), "some-path");
  }

}

