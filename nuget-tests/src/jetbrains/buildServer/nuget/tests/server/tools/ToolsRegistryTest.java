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
import jetbrains.buildServer.nuget.server.toolRegistry.impl.PluginNaming;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolsRegistry;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolsWatcher;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

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
      allowing(path).getTools(); will(returnValue(myToolsHome));
      allowing(path).getPackages(); will(returnValue(myPackagesHome));
      allowing(path).getAgentPluginsPath(); will(returnValue(myAgentHome));
    }});

    myRegistry = new ToolsRegistry(path, new PluginNaming(path), myWatcher);
  }


  @Test
  public void test_empty() {
    Assert.assertTrue(myRegistry.getTools().isEmpty());
  }


  private void file(@NotNull File root, @NotNull String path) {
    final File dest = new File(root, path);
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "2qwerwer" + path);
  }

  @Test
  public void test_one_package() {
    nupkg("NuGet.CommandLine.1.2.3.4");
    Assert.assertEquals(myRegistry.getTools().size(), 1);
  }

  private void nupkg(String name) {
    file(myPackagesHome, name + ".nupkg");
    file(myAgentHome, name + ".nupkg.zip");
    file(myToolsHome, name + ".nupkg/tools/nuget.exe");
  }

  @Test
  public void test_two_packages() {
    nupkg("NuGet.CommandLine.1.2.3.4");
    nupkg("NuGet.CommandLine.1.3.3.4");
    nupkg("NuGet.CommandLine.1.2.6.4");
    nupkg("NuGet.CommandLine.2.2.3.4.z");
    nupkg("NuGet.CommandLine.2.2.3.14.y");
    nupkg("NuGet.aaaamandLine.4.4.4");

    final Collection<? extends NuGetInstalledTool> tools = myRegistry.getTools();
    final Iterator<String> versions = Arrays.asList("1.2.3.4", "1.2.6.4", "1.3.3.4", "2.2.3.4", "2.2.3.14", "4.4.4").iterator();

    for (NuGetInstalledTool tool : tools) {
      System.out.println("tool.getVersion() = " + tool.getVersion());
      final String v = versions.next();
      Assert.assertEquals(tool.getVersion(), v);
    }
  }


}
