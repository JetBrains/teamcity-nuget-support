/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.runner.install;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.NuGetTools;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunnerDiscoverer;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.browser.FileSystemBrowser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesInstallerRunnerDiscovererTest extends BaseTestCase {

  private final String myDefaultToolPath = NuGetTools.getDefaultToolPath();
  private PackagesInstallerRunnerDiscoverer myDiscoverer;
  private BuildTypeSettings myBuildTypeSettings;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myBuildTypeSettings = (BuildTypeSettings) mock(BuildTypeSettings.class).proxy();
    myDiscoverer = new PackagesInstallerRunnerDiscoverer();
  }

  @Test
  public void singleSolutionWithDotNugetDirectory() throws Exception {
    final File rootPath = getTestDataPath("single-sln-dot-nuget").getAbsoluteFile();
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, new FileSystemBrowser(rootPath));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "foo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesInstallerRunnerDiscoverer.CHECKED);
  }

  @Test
  public void singleSolutionWithPackagesConfigInTheSameDirectory() throws Exception {
    final File rootPath = getTestDataPath("single-sln-packages-config").getAbsoluteFile();
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, new FileSystemBrowser(rootPath));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "foo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesInstallerRunnerDiscoverer.CHECKED);
  }

  @Test
  public void severalSolutionsOneLevel() throws Exception {
    final File rootPath = getTestDataPath("several-sln-one-level").getAbsoluteFile();
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, new FileSystemBrowser(rootPath));
    assertNotNull(runners);
    assertEquals(2, runners.size());
  }

  @Test
  public void severalSolutionsTwoLevels() throws Exception {
    final File rootPath = getTestDataPath("several-sln-two-levels").getAbsoluteFile();
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, new FileSystemBrowser(rootPath));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "foo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesInstallerRunnerDiscoverer.CHECKED);
  }

  @Test
  public void singleSolutionSecondLevel() throws Exception {
    final File rootPath = getTestDataPath("single-sln-second-level").getAbsoluteFile();
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, new FileSystemBrowser(rootPath));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.INSTALL_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PATH, myDefaultToolPath);
    assertMapping(runnerParameters, PackagesConstants.SLN_PATH, "dir/boo.sln");
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesInstallerRunnerDiscoverer.CHECKED);
  }

  private File getTestDataPath(String relativepath) {
    final File testDataPath = new File(Paths.getTestDataPath("discovery"), relativepath);
    assertTrue("Test data path was not found on path " + testDataPath.getPath(), testDataPath.isDirectory());
    return testDataPath;
  }
}
