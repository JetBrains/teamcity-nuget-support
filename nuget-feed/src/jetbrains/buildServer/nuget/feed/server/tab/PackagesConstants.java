/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.tab;

import jetbrains.buildServer.serverSide.packages.RepositoryConstants;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 31.10.11 11:56
 */
public class PackagesConstants {
  public static final String NUGET_SERVER_ENABLED_CHECKBOX = "nugetServer.enabled";
  public static final String NUGET_SERVER_URL = "nugetServer.url";

  public String getNugetServerEnabledCheckbox() {
    return NUGET_SERVER_ENABLED_CHECKBOX;
  }

  public String getNugetServerUrl() {
    return NUGET_SERVER_URL;
  }

  public String getType() {
      return RepositoryConstants.REPOSITORY_TYPE_KEY;
  }

  public String getName() {
      return RepositoryConstants.REPOSITORY_NAME_KEY;
  }

  public String getDescription() {
      return RepositoryConstants.REPOSITORY_DESCRIPTION_KEY;
  }
}
