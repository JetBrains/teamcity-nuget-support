/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.NuGetToolsInstaller;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolsWatcher;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.NuGetToolsInstallerImpl;
import jetbrains.buildServer.nuget.tests.Strings;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 1:59
 */
public class NuGetToolsInstallerTest extends BaseTestCase {
  private NuGetToolsInstaller myInstaller;
  private ToolPaths myPaths;
  private ToolsWatcher myWatcher;
  private Mockery m;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();
    myPaths = m.mock(ToolPaths.class);
    myWatcher = m.mock(ToolsWatcher.class);

    myInstaller = new NuGetToolsInstallerImpl(myPaths, myWatcher);

    m.checking(new Expectations(){{
      allowing(myPaths).getNuGetToolsPath(); will(returnValue(createTempDir()));
      allowing(myPaths).getNuGetToolsAgentPluginsPath(); will(returnValue(createTempDir()));
      allowing(myPaths).getNuGetToolsPackages(); will(returnValue(createTempDir()));
    }});
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    m.assertIsSatisfied();
    super.tearDown();
  }

  @Test
  public void testUploadKnownFile() throws ToolException, IOException {
    m.checking(new Expectations(){{
      oneOf(myWatcher).checkNow();
    }});

    myInstaller.installNuGet("NuGet.1.2.3.nupkg", getNuGetPackageFile());
    assertTrue(new File(myPaths.getNuGetToolsPackages(), "NuGet.1.2.3.nupkg").isFile());
  }

  @Test(expectedExceptions = ToolException.class)
  public void testFeedFile_invalidNupkgFile() throws ToolException, IOException {
    final File tempFile = createTempFile();
    FileUtil.writeFileAndReportErrors(tempFile, Strings.EXOTIC);
    myInstaller.installNuGet("packageId.nupkg", tempFile);
  }

  @Test
  public void testFeedFile_invalidExeFile() throws ToolException, IOException {
    m.checking(new Expectations(){{
      oneOf(myWatcher).checkNow();
    }});

    final File tempFile = createTempFile();
    FileUtil.writeFileAndReportErrors(tempFile, Strings.EXOTIC);
    myInstaller.installNuGet("packageId.exe", tempFile);
  }

  @NotNull
  private File getNuGetPackageFile() throws IOException {
    File home = createTempFile();
    FileUtil.copy(new File("./nuget-tests/testData/nuget/NuGet.CommandLine.1.8.40002.nupkg"), home);
    return home;
  }
}
