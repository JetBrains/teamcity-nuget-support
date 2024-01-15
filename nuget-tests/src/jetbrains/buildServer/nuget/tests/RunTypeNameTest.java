

package jetbrains.buildServer.nuget.tests;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunType;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunnerDefaults;
import jetbrains.buildServer.nuget.server.runner.pack.PackRunType;
import jetbrains.buildServer.nuget.server.runner.publish.PublishRunType;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 19:03
 */
public class RunTypeNameTest extends BaseTestCase {
  private PluginDescriptor myDescriptor;
  private PackagesInstallerRunnerDefaults myDefaults;
  private ServerToolManager myToolManager;
  protected ProjectManager myProjectManager;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Mockery m = new Mockery();
    myDescriptor = m.mock(PluginDescriptor.class);
    myToolManager = m.mock(ServerToolManager.class);
    myProjectManager = m.mock(ProjectManager.class);
    myDefaults = new PackagesInstallerRunnerDefaults();
  }

  @Test
  public void test_installPackagesRunTypeIdLendth() {
    final String type = new PackagesInstallerRunType(myDescriptor, myDefaults, myToolManager, myProjectManager).getType();
    Assert.assertTrue(type.length() < 30);
  }

  @Test
  public void test_packRunTypeIdLendth() {
    final String type = new PackRunType(myDescriptor, myToolManager, myProjectManager).getType();
    Assert.assertTrue(type.length() < 30);
  }

  @Test
  public void test_publishPackagesRunTypeIdLendth() {
    final String type = new PublishRunType(myDescriptor, myToolManager, myProjectManager).getType();
    Assert.assertTrue(type.length() < 30);
  }
}
