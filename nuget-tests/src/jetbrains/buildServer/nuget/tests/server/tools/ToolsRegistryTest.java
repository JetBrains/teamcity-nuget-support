/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.*;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.ToolsRegistryImpl;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 17:07
 */
public class ToolsRegistryTest extends BaseTestCase {
  private Mockery m;
  private File myToolsHome;
  private File myPackagesHome;
  private File myAgentHome;
  private ToolsRegistry myRegistry;
  private ToolsWatcher myWatcher;
  private PluginNaming myNaming;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();
    myToolsHome = createTempDir();
    myPackagesHome = createTempDir();
    myAgentHome = createTempDir();

    final ToolPaths path = m.mock(ToolPaths.class);
    myWatcher = m.mock(ToolsWatcher.class);

    m.checking(new Expectations(){{
      allowing(path).getNuGetToolsPath(); will(returnValue(myToolsHome));
      allowing(path).getNuGetToolsPackages(); will(returnValue(myPackagesHome));
      allowing(path).getNuGetToolsAgentPluginsPath(); will(returnValue(myAgentHome));
    }});
    myNaming = new PluginNaming(path);
    myRegistry = new ToolsRegistryImpl(path, myNaming, myWatcher);
  }


  @Test
  public void test_empty() {
    Assert.assertTrue(myRegistry.getTools().isEmpty());
  }

  @NotNull
  private File file(@NotNull File root, @NotNull String path) {
    final File dest = new File(root, path);
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "2qwerwer" + path);
    return dest;
  }

  @Test
  public void test_one_package() {
    nupkg("NuGet.CommandLine.1.2.3.4");
    Assert.assertEquals(myRegistry.getTools().size(), 1);
  }

  private InstalledTool nupkg(String name) {
    File pkg = file(myPackagesHome, name + ".nupkg");
    file(myAgentHome, name + ".nupkg.zip");
    file(myToolsHome, name + ".nupkg/tools/nuget.exe");
    return new InstalledTool(myNaming, pkg);
  }

  @Test
  public void test_two_packages() {
    nupkg("NuGet.CommandLine.1.2.3.4");
    nupkg("NuGet.CommandLine.1.3.3.4");
    nupkg("NuGet.CommandLine.1.2.6.4");
    nupkg("NuGet.CommandLine.2.2.3.4.z");
    nupkg("NuGet.CommandLine.2.2.3.14.y");
    nupkg("NuGet.aaaamandLine.4.4.4");

    assertPackageNames("NuGet.aaaamandLine.4.4.4", "1.2.3.4", "1.2.6.4", "1.3.3.4", "2.2.3.4.z", "2.2.3.14.y");
  }

  @Test
  public void test_package_with_error() {
    m.checking(new Expectations(){{
      oneOf(myWatcher).updatePackage(with(any(InstalledTool.class)));
    }});

    InstalledTool nupkg = nupkg("NuGet.CommandLine.1.2.3.4");
    FileUtil.delete(nupkg.getAgentPluginFile());
    assertPackageNames();

    //call once more to assert no cleanup is performed
    myRegistry.getTools();

    m.assertIsSatisfied();
  }

  @Test
  public void test_package_with_error_recover() {
    test_package_with_error();
    nupkg("NuGet.CommandLine.1.2.3.4");
    assertPackageNames("1.2.3.4");
  }

  private void assertPackageNames(String... goldNames) {
    final Collection<? extends NuGetInstalledTool> tools = myRegistry.getTools();
    final List<String> names = new ArrayList<String>();
    for (NuGetInstalledTool tool : tools) {
      System.out.println("tool.getVersion() = " + tool.getVersion());
      names.add(tool.getVersion());
    }
    Assert.assertEquals(names, Arrays.asList(goldNames));
  }
}
