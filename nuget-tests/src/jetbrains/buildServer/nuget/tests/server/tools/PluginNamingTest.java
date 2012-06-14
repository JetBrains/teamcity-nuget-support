/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.server.impl.ToolPathsImpl;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.PluginNaming;
import jetbrains.buildServer.serverSide.ServerPaths;
import junit.framework.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 14.06.12 20:20
 */
public class PluginNamingTest extends BaseTestCase {
  private PluginNaming myNaming;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myNaming = new PluginNaming(new ToolPathsImpl(new ServerPaths(createTempDir().getPath())));
  }


  @Test
  public void testPackageName() {
    Assert.assertEquals(myNaming.getVersion(new File("c:\\NuGet.CommandLine.1.2.3.nupkg")), "1.2.3");
    Assert.assertEquals(myNaming.getVersion(new File("c:\\NuGet.CommandLine.2.3.4.5.nupkg")), "2.3.4.5");
    Assert.assertEquals(myNaming.getVersion(new File("c:\\NuGet.CommandLine.2.3.4.5-alpha.nupkg")), "2.3.4.5-alpha");
    Assert.assertEquals(myNaming.getVersion(new File("c:\\NuGet.CommandLine.2.3.4.5-alpha.zip")), "NuGet.CommandLine.2.3.4.5-alpha.zip");
    Assert.assertEquals(myNaming.getVersion(new File("c:\\NuGet.Hack.2.3.4.5-alpha.zip")), "NuGet.Hack.2.3.4.5-alpha.zip");
    Assert.assertEquals(myNaming.getVersion(new File("c:\\NuGet.Hack.2.3.4.5-alpha.nupkg")), "NuGet.Hack.2.3.4.5-alpha");
  }
}
