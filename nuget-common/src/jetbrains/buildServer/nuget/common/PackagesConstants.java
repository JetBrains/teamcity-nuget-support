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

package jetbrains.buildServer.nuget.common;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 13:56
 */
public interface PackagesConstants {
  public static final String RUN_TYPE = "jetbrains.nuget.packagesInstaller";


  public static final String NUGET_PATH = "nuget.path";
  public static final String NUGET_SOURCES = "nuget.sources";
  public static final String NUGET_EXCLUDE_VERSION = "nuget.excludeVersion";

  public static final String NUGET_UPDATE_PACKAGES = "nuget.updatePackages";
  public static final String NUGET_UPDATE_PACKAGES_SAFE = "nuget.updatePackages.safe";
  public static final String NUGET_UPDATE_PACKAGE_IDS = "nuget.updatePackages.ids";
  public static final String NUGET_UPDATE_MODE = "nuget.updatePackages.mode";

  public static final String SLN_PATH = "sln.path";
}
