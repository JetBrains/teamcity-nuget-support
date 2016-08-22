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

import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunType;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunnerDefaults;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 16.03.12 13:38
 */
public class PackagesInstallerRunTypeTest extends NuGetRunTypeTest<PackagesInstallerRunType> {
  @NotNull
  protected PackagesInstallerRunType createRunType() {
    final ServerToolManager toolManager = (ServerToolManager) mock(ServerToolManager.class).proxy();
    return new PackagesInstallerRunType(myDescriptor, new PackagesInstallerRunnerDefaults(), toolManager, myProjectManager);
  }

  @Test
  @TestFor(issues = "TW-20702")
  public void testParametersValidator_slnPath_reference() {
    doTestValidator(m("nuget.path", "fpo", "sln.path", "%solution%"), s());
  }

  @Test
  @TestFor(issues = "TW-20702")
  public void testParametersValidator_ok() {
    doTestValidator(m("nuget.path", "fpo", "sln.path", "file.sln"), s());
  }

  @Test
  @TestFor(issues = "TW-20702")
  public void testParametersValidator_not_sln() {
    doTestValidator(m("nuget.path", "fpo", "sln.path", "file.s"), s("sln.path"));
  }

  @Test
  @TestFor(issues = "TW-20702")
  public void testParametersValidator_empty() {
    doTestValidator(m(), s("nuget.path", "sln.path"));
  }
}
