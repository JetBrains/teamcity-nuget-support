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

import java.util.Map;
import java.util.TreeMap;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_USE_RESTORE_COMMAND;
import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_USE_RESTORE_COMMAND_RESTORE_MODE;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesInstallerRunnerDefaults {
  public Map<String,String> getRunnerProperties(){
    final TreeMap<String, String> map = new TreeMap<String, String>();
    map.put(NUGET_USE_RESTORE_COMMAND, NUGET_USE_RESTORE_COMMAND_RESTORE_MODE);
    return map;
  }
}
