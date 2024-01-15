

package jetbrains.buildServer.nuget.common;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_USE_RESTORE_COMMAND_INSTALL_MODE;
import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_USE_RESTORE_COMMAND_RESTORE_MODE;

/**
 * Created 13.08.13 13:48
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public enum PackagesInstallMode {
  /**
   * This method calls one NuGet command to update all packages
   */
  VIA_RESTORE(NUGET_USE_RESTORE_COMMAND_RESTORE_MODE),

  /**
   * This methos must call a NuGet udpate command per packages.config
   */
  VIA_INSTALL(NUGET_USE_RESTORE_COMMAND_INSTALL_MODE),
  ;
  private final String myName;

  PackagesInstallMode(@NotNull final String name) {
    myName = name;
  }

  @NotNull
  public String getName() {
    return myName;
  }


  @Override
  public String toString() {
    return myName;
  }

  @Nullable
  public static PackagesInstallMode parse(@Nullable String text) {
    //convertion for older settings
    if (StringUtil.isEmptyOrSpaces(text)) return VIA_INSTALL;

    for (PackagesInstallMode mode : values()) {
      if (mode.getName().equals(text)) return mode;
    }
    return null;
  }
}
