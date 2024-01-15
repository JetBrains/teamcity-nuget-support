

package jetbrains.buildServer.nuget.server.runner.install;

import jetbrains.buildServer.nuget.common.*;
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
  public String getRestoreCustomCommandline() { return PackagesConstants.NUGET_RESTORE_CUSOM_COMMANDLINE; }
  public String getUpdateCustomCommandline() { return PackagesConstants.NUGET_UPDATE_CUSOM_COMMANDLINE; }
  public String getNugetToolTypeName() {return FeedConstants.NUGET_COMMANDLINE;}
}
