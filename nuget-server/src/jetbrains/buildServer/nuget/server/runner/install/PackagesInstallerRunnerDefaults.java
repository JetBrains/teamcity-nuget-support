

package jetbrains.buildServer.nuget.server.runner.install;

import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;

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
    map.put(PackagesConstants.NUGET_UPDATE_MODE, PackagesUpdateMode.FOR_SLN.getName());
    return map;
  }
}
