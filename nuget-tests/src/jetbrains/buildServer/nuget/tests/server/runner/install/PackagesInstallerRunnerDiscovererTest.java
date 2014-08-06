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

import jetbrains.buildServer.nuget.common.NuGetToolReferenceUtils;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunnerDiscoverer;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.serverSide.impl.SBuildRunnerDescriptorImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesInstallerRunnerDiscovererTest extends NuGetRunnerDiscovererTestBase {

  private final String myDefaultToolPath = NuGetToolReferenceUtils.getDefaultToolPath();
  private PackagesInstallerRunnerDiscoverer myDiscoverer;

  public PackagesInstallerRunnerDiscovererTest() {
    super("install");
  }

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myDiscoverer = new PackagesInstallerRunnerDiscoverer();
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
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesInstallMode.VIA_RESTORE.getName());
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
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesInstallMode.VIA_RESTORE.getName());
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
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesInstallMode.VIA_RESTORE.getName());
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
    assertMapping(runnerParameters, PackagesConstants.NUGET_USE_RESTORE_COMMAND, PackagesInstallMode.VIA_RESTORE.getName());
  }

  @Test
  public void ignoreAlreadyAddedRunners() throws Exception {
    registerNugetInstallerRunner("foo.sln");
    assertEmpty(myDiscoverer.discover(myBuildTypeSettings, getBrowser("single-sln-dot-nuget")));
  }

  private void registerNugetInstallerRunner(String pathToSlnFile) {
    myRegisteredRunners.add(new SBuildRunnerDescriptorImpl("id", "name", PackagesConstants.INSTALL_RUN_TYPE, Collections.singletonMap(PackagesConstants.SLN_PATH, pathToSlnFile), Collections.<String, String>emptyMap(), myRunTypesProvider));
  }
}
