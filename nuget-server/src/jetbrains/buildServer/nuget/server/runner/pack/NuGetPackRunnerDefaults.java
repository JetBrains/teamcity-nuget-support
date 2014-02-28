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

package jetbrains.buildServer.nuget.server.runner.pack;

import jetbrains.buildServer.agent.ServerProvidedProperties;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.common.PackagesPackDirectoryMode;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackRunnerDefaults {
  private static final String DEFAULT_PACK_PROPS = "Configuration=Release";
  private static final String CHECKED = "checked";

  public static Map<String,String> getRunnerProperties() {
    return new HashMap<String, String>(){{
      put(PackagesConstants.NUGET_PACK_VERSION, "0." + ReferencesResolverUtil.makeReference(ServerProvidedProperties.BUILD_NUMBER_PROP));
      put(PackagesConstants.NUGET_PACK_OUTPUT_CLEAR, CHECKED);
      put(PackagesConstants.NUGET_PACK_PROPERTIES, DEFAULT_PACK_PROPS);
      put(PackagesConstants.NUGET_PACK_BASE_DIRECTORY_MODE, PackagesPackDirectoryMode.LEAVE_AS_IS.getValue());
    }};
  }
}
