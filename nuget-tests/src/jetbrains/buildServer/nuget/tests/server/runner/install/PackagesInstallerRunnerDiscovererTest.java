

package jetbrains.buildServer.nuget.tests.server.runner.install;

import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunnerDefaults;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunnerDiscoverer;
import jetbrains.buildServer.nuget.server.tool.NuGetServerToolProvider;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.serverSide.impl.SBuildRunnerDescriptorImpl;
import jetbrains.buildServer.tools.ToolVersionReference;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesInstallerRunnerDiscovererTest extends NuGetRunnerDiscovererTestBase {

  private final String myDefaultToolPath = ToolVersionReference.getDefaultToolReference(NuGetServerToolProvider.NUGET_TOOL_TYPE.getType()).getReference();
  private PackagesInstallerRunnerDiscoverer myDiscoverer;

  public PackagesInstallerRunnerDiscovererTest() {
    super("install");
  }

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myDiscoverer = new PackagesInstallerRunnerDiscoverer(new PackagesInstallerRunnerDefaults());
  }

  @Test
  public void solutionFileNotFound() throws Exception {
    assertEmpty(myDiscoverer.discover(myBuildTypeSettings, getBrowser("no-sln")));
  }

  @Test
  public void nugetUsageNotFound() throws Exception {
    assertEmpty(myDiscoverer.discover(myBuildTypeSettings, getBrowser("no-nuget")));
  }

  @Test
  public void singleSolutionWithDotNugetDirectory() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("single-sln-dot-nuget"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "foo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesConstants.NUGET_USE_RESTORE_COMMAND_RESTORE_MODE);
  }

  @Test
  public void singleSolutionWithPackagesConfigInTheSameDirectory() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("single-sln-packages-config"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "foo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesConstants.NUGET_USE_RESTORE_COMMAND_RESTORE_MODE);
  }

  @Test
  public void severalSolutionsOneLevel() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("several-sln-one-level"));
    assertNotNull(runners);
    assertEquals(2, runners.size());
  }

  @Test
  public void severalSolutionsTwoLevels() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("several-sln-two-levels"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "foo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesConstants.NUGET_USE_RESTORE_COMMAND_RESTORE_MODE);
  }

  @Test
  public void singleSolutionSecondLevel() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("single-sln-second-level"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "dir/boo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesConstants.NUGET_USE_RESTORE_COMMAND_RESTORE_MODE);
  }

  @Test
  public void ignoreAlreadyAddedRunners() throws Exception {
    registerNugetInstallerRunner("foo.sln");
    assertEmpty(myDiscoverer.discover(myBuildTypeSettings, getBrowser("single-sln-dot-nuget")));
  }

  @Test
  public void solutionWithPackageReferences() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("sln-with-package-references"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "foo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesConstants.NUGET_USE_RESTORE_COMMAND_RESTORE_MODE);
  }

  @Test
  public void solutionWithProjectPackages() {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("sln-with-project-packages"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "foo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesConstants.NUGET_USE_RESTORE_COMMAND_RESTORE_MODE);
  }

  private void registerNugetInstallerRunner(String pathToSlnFile) {
    myRegisteredRunners.add(new SBuildRunnerDescriptorImpl("id", "name", PackagesConstants.INSTALL_RUN_TYPE, Collections.singletonMap(PackagesConstants.SLN_PATH, pathToSlnFile), Collections.<String, String>emptyMap(), myRunTypesProvider));
  }
}
