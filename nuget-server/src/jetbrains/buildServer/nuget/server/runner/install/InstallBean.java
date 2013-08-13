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

package jetbrains.buildServer.nuget.server.runner.install;

import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 08.07.11 20:49
 */
public class InstallBean {
  public String getNuGetPathKey() { return PackagesConstants.NUGET_PATH; }
  public String getNuGetSourcesKey() { return PackagesConstants.NUGET_SOURCES; }
  public String getSolutionPathKey() { return PackagesConstants.SLN_PATH; }
  public String getExcludeVersionKey() { return PackagesConstants.NUGET_EXCLUDE_VERSION;}
  public String getRestoreCommandModeKey() { return PackagesConstants.NUGET_USE_RESTORE_COMMAND;}
  public String getRestoreCommandModeRestoreValue() { return PackagesInstallMode.VIA_RESTORE.getName();}
  public String getRestoreCommandModeInstallValue() { return PackagesInstallMode.VIA_INSTALL.getName();}
  public String getNoCacheKey() { return PackagesConstants.NUGET_NO_CACHE; }
  public String getUpdatePackagesKey() { return PackagesConstants.NUGET_UPDATE_PACKAGES;}
  public String getUpdatePackagesSafeKey() { return PackagesConstants.NUGET_UPDATE_PACKAGES_SAFE;}
  public String getUpdatePackagesPrerelease() { return PackagesConstants.NUGET_UPDATE_PACKAGES_PRERELEASE;}
  public String getUpdateModeKey() { return PackagesConstants.NUGET_UPDATE_MODE;}
  public String getUpdatePerSolutionValue() { return PackagesUpdateMode.FOR_SLN.getName();}
  public String getUpdatePerConfigValue() { return PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG.getName();}
  public String getNuGetFeedReference() { return ReferencesResolverUtil.makeReference(NuGetServerConstants.FEED_REFERENCE); }
  public String getNuGetAuthFeedReference() { return ReferencesResolverUtil.makeReference(NuGetServerConstants.FEED_AUTH_REFERENCE); }
}
