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

package jetbrains.buildServer.nuget.tests.server.runner.install;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunnerDefaults;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mock;
import org.jmock.core.matcher.AnyArgumentsMatcher;
import org.testng.annotations.Test;

import java.io.File;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_USE_RESTORE_COMMAND;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesInstallerRunnerDefaultsTest extends BaseTestCase {
  private PackagesInstallerRunnerDefaults myDefaults;

  @Test
  public void doNotUseRestoreCommandIfNoDefaultToolSpecified() throws Exception {
    setUpDefaults(null);
    assertMapping(myDefaults.getRunnerProperties(scope), NUGET_USE_RESTORE_COMMAND, PackagesInstallerRunnerDefaults.CHECKED);
  }

  @Test
  public void useRestoreCommandIfDefaultNuGetIs27OrNewer() throws Exception {
    final NuGetInstalledTool defaultTool = new NuGetInstalledTool() {
      @NotNull
      public File getNuGetExePath() {
        return new File("");
      }

      public boolean isDefaultTool() {
        return true;
      }

      @NotNull
      public String getId() {
        return "id";
      }

      @NotNull
      public String getVersion() {
        return "2.7.1";
      }
    };
    setUpDefaults(defaultTool);
    assertMapping(myDefaults.getRunnerProperties(scope), NUGET_USE_RESTORE_COMMAND, PackagesInstallerRunnerDefaults.CHECKED);
  }

  @Test
  public void doNotUseRestoreCommandIfDefaultNuGetIs26OrOlder() throws Exception {
    final NuGetInstalledTool defaultTool = new NuGetInstalledTool() {
      @NotNull
      public File getNuGetExePath() {
        return new File("");
      }

      public boolean isDefaultTool() {
        return true;
      }

      @NotNull
      public String getId() {
        return "id";
      }

      @NotNull
      public String getVersion() {
        return "2.5.0";
      }
    };
    setUpDefaults(defaultTool);
    assertFalse(myDefaults.getRunnerProperties(scope).containsKey(NUGET_USE_RESTORE_COMMAND));
  }

  private void setUpDefaults(NuGetInstalledTool defaultTool) {
    final Mock toolManagerMock = mock(NuGetToolManager.class);
    toolManagerMock.expects(new AnyArgumentsMatcher()).method("getDefaultTool").will(returnValue(defaultTool));
    final NuGetToolManager toolManager = (NuGetToolManager) toolManagerMock.proxy();
    myDefaults = new PackagesInstallerRunnerDefaults(toolManager);
  }
}
