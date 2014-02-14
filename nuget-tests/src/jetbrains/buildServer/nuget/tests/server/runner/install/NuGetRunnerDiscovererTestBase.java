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
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.RunTypesProvider;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.util.browser.FileSystemBrowser;
import org.jmock.Mock;
import org.jmock.core.matcher.AnyArgumentsMatcher;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetRunnerDiscovererTestBase extends BaseTestCase {

  protected BuildTypeSettings myBuildTypeSettings;
  protected RunTypesProvider myRunTypesProvider;
  protected List<SBuildRunnerDescriptor> myRegisteredRunners;

  private File myTestsHome;

  public NuGetRunnerDiscovererTestBase(String runnerType) {
    myTestsHome = new File(Paths.getTestDataPath("discovery"), runnerType);
  }

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myRegisteredRunners = new ArrayList<SBuildRunnerDescriptor>();
    final Mock buildTypeSettingsMock = mock(BuildTypeSettings.class);
    buildTypeSettingsMock.expects(new AnyArgumentsMatcher()).method("getBuildRunners").will(returnValue(myRegisteredRunners));
    myBuildTypeSettings = (BuildTypeSettings) buildTypeSettingsMock.proxy();
    myRunTypesProvider = (RunTypesProvider) mock(RunTypesProvider.class).proxy();
  }

  protected FileSystemBrowser getBrowser(String relativePath) {
    return new FileSystemBrowser(getTestDataPath(relativePath).getAbsoluteFile());
  }

  private File getTestDataPath(String relativepath) {
    final File testDataPath = new File(myTestsHome, relativepath);
    assertTrue("Test data path was not found on path " + testDataPath.getPath(), testDataPath.isDirectory());
    return testDataPath;
  }
}
