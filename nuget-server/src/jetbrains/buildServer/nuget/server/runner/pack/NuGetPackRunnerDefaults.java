

package jetbrains.buildServer.nuget.server.runner.pack;

import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.common.PackagesPackDirectoryMode;

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
      put(PackagesConstants.NUGET_PACK_OUTPUT_CLEAR, CHECKED);
      put(PackagesConstants.NUGET_PACK_PROPERTIES, DEFAULT_PACK_PROPS);
      put(PackagesConstants.NUGET_PACK_BASE_DIRECTORY_MODE, PackagesPackDirectoryMode.LEAVE_AS_IS.getValue());
    }};
  }
}
