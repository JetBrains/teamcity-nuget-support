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
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.*;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.InstalledToolsFactory;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.ToolsRegistryImpl;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
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

    final PluginNaming naming = new PluginNaming(path);
    myRegistry = new ToolsRegistryImpl(path, myWatcher, new InstalledToolsFactory(naming, new ToolPacker(), new ToolUnpacker()));
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    m.assertIsSatisfied();
    super.tearDown();
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
    tool("NuGet.CommandLine.1.2.3.4");
    Assert.assertEquals(myRegistry.getTools().size(), 1);
  }

  @Test
  public void test_two_packages() {
    tool("NuGet.CommandLine.1.2.3.4");
    tool("NuGet.CommandLine.1.3.3.4");
    tool("NuGet.CommandLine.1.2.6.4");
    tool("NuGet.CommandLine.2.2.3.4.z");
    tool("NuGet.CommandLine.2.2.3.14.y");
    tool("NuGet.aaaamandLine.4.4.4");

    assertPackageNames("NuGet.aaaamandLine.4.4.4", "1.2.3.4", "1.2.6.4", "1.3.3.4", "2.2.3.4.z", "2.2.3.14.y");
  }

  @Test
  public void test_package_with_error() {
    m.checking(new Expectations(){{
      oneOf(myWatcher).updateTool(with(any(InstalledTool.class)));
    }});

    InstalledTool nupkg = tool("NuGet.CommandLine.1.2.3.4");
    FileUtil.delete(nupkg.getAgentPluginFile());
    assertPackageNames();

    //call once more to assert no cleanup is performed
    myRegistry.getTools();
  }

  @Test
  public void test_package_with_error_recover() {
    test_package_with_error();
    tool("NuGet.CommandLine.1.2.3.4");
    assertPackageNames("1.2.3.4");
  }

  private InstalledTool tool(String name) {
    file(myPackagesHome, name + ".nupkg");
    file(myAgentHome, name + ".nupkg.zip");
    file(myToolsHome, name + ".nupkg/tools/nuget.exe");
    return new InstalledTool() {
      @NotNull
      public File getAgentPluginFile() {
        return myAgentHome;
      }

      public void install() {
      }

      public void delete() {
      }

      @NotNull
      public File getNuGetExePath() {
        return null;
      }

      public boolean isDefaultTool() {
        return false;
      }

      @NotNull
      public String getId() {
        return null;
      }

      @NotNull
      public String getVersion() {
        return null;
      }
    };
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
