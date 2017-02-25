/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.runner.pack.PackRunnerDiscoverer;
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
public class PackRunnerDiscovererTest extends NuGetRunnerDiscovererTestBase {
  private PackRunnerDiscoverer myDiscoverer;

  public PackRunnerDiscovererTest() {
    super("pack");
  }

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myDiscoverer = new PackRunnerDiscoverer();
  }

  @Test
  public void noNuSpecFilesFound() throws Exception {
    assertEmpty(myDiscoverer.discover(myBuildTypeSettings, getBrowser("no-nuspec")));
  }

  @Test
  public void nuSpecFileFound() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("nuspec-found"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.PACK_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PACK_SPEC_FILE, "Package.nuspec");
    assertMapping(runnerParameters, PackagesConstants.NUGET_PACK_OUTPUT_DIR, PackRunnerDiscoverer.DEFAULT_OUT_DIR_PATH);
  }

  @Test
  public void severalNuSpecFilesInOneDir() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("several-nuspec-one-dir"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.PACK_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PACK_SPEC_FILE, "Package1.nuspec\nPackage2.nuspec");
  }

  @Test
  public void severalNuSpecFilesInSeveralDirs() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("several-nuspec-several-dirs"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.PACK_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PACK_SPEC_FILE, "Package1.nuspec\nPackage2.nuspec");
  }

  @Test
  public void outputDirectoryNameCollision() throws Exception {
    final List<DiscoveredObject> runners = myDiscoverer.discover(myBuildTypeSettings, getBrowser("out-dir-collision"));
    assertNotNull(runners);
    assertEquals(1, runners.size());
    final DiscoveredObject runner = runners.get(0);
    assertEquals(PackagesConstants.PACK_RUN_TYPE, runner.getType());
    final Map<String, String> runnerParameters = runner.getParameters();
    assertMapping(runnerParameters, PackagesConstants.NUGET_PACK_SPEC_FILE, "Package.nuspec");
    assertTrue(runnerParameters.containsKey(PackagesConstants.NUGET_PACK_OUTPUT_DIR));
    assertNotSame(PackRunnerDiscoverer.DEFAULT_OUT_DIR_PATH, runnerParameters.get(PackagesConstants.NUGET_PACK_OUTPUT_DIR));
  }

  @Test
  public void ignoreAlreadyAddedRunners() throws Exception {
    registerNugetPackRunner("Package.nuspec");
    assertEmpty(myDiscoverer.discover(myBuildTypeSettings, getBrowser("nuspec-found")));
  }

  private void registerNugetPackRunner(String pathToNuSpecFile) {
    myRegisteredRunners.add(new SBuildRunnerDescriptorImpl("id", "name", PackagesConstants.PACK_RUN_TYPE, Collections.singletonMap(PackagesConstants.NUGET_PACK_SPEC_FILE, pathToNuSpecFile), Collections.<String, String>emptyMap(), myRunTypesProvider));
  }
}
