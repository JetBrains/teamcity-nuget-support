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

package jetbrains.buildServer.nuget.server.runner.install;

import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_USE_RESTORE_COMMAND;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesInstallerRunnerDefaults {

  private static final String FIRST_NUGET_VERSION_WITH_RESTORE_CMD = "2.7";
  public static final String CHECKED = "checked";

  @NotNull private final NuGetToolManager myToolManager;

  public PackagesInstallerRunnerDefaults(@NotNull final NuGetToolManager toolManager) {
    myToolManager = toolManager;
  }

  public Map<String,String> getRunnerProperties(){
    final TreeMap<String, String> map = new TreeMap<String, String>();
    final NuGetInstalledTool defaultNugetInstalled = myToolManager.getDefaultTool();
    if (defaultNugetInstalled == null) {
      map.put(NUGET_USE_RESTORE_COMMAND, CHECKED);
    } else if (defaultNugetInstalled.getVersion().compareTo(FIRST_NUGET_VERSION_WITH_RESTORE_CMD) >= 0) {
      map.put(NUGET_USE_RESTORE_COMMAND, PackagesInstallMode.VIA_RESTORE.getName());
    } else {
      map.put(NUGET_USE_RESTORE_COMMAND, PackagesInstallMode.VIA_INSTALL.getName());
    }

    return map;
  }
}
