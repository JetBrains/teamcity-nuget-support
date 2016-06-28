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

package jetbrains.buildServer.nuget.tests;

import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunType;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunnerDefaults;
import jetbrains.buildServer.nuget.server.runner.pack.PackRunType;
import jetbrains.buildServer.nuget.server.runner.publish.PublishRunType;
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 19:03
 */
public class RunTypeNameTest extends BaseServerTestCase {
  private PluginDescriptor myDescriptor;
  private PackagesInstallerRunnerDefaults myDefaults;
  private ServerToolManager myToolManager;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Mockery m = new Mockery();
    myDescriptor = m.mock(PluginDescriptor.class);
    myToolManager = m.mock(ServerToolManager.class);
    myProjectManager = myFixture.getProjectManager();
    myDefaults = new PackagesInstallerRunnerDefaults();
  }

  @Test
  public void test_installPackagesRunTypeIdLendth() {
    final String type = new PackagesInstallerRunType(myDescriptor, myDefaults, myToolManager, myProjectManager).getType();
    Assert.assertTrue(type.length() < 30);
  }

  @Test
  public void test_packRunTypeIdLendth() {
    final String type = new PackRunType(myDescriptor, myToolManager, myProjectManager).getType();
    Assert.assertTrue(type.length() < 30);
  }

  @Test
  public void test_publishPackagesRunTypeIdLendth() {
    final String type = new PublishRunType(myDescriptor, myToolManager, myProjectManager).getType();
    Assert.assertTrue(type.length() < 30);
  }
}
