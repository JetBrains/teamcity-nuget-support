

package jetbrains.buildServer.nuget.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 19:49
 */
public enum PackagesUpdateMode {
  /**
   * This method calls one NuGet command to update all packages
   */
  FOR_SLN("sln"),

  /**
   * This methos must call a NuGet udpate command per packages.config
   */
  FOR_EACH_PACKAGES_CONFIG("perConfig"),
  ;
  private final String myName;

  PackagesUpdateMode(@NotNull final String name) {
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
  public static PackagesUpdateMode parse(@Nullable String text) {
    for (PackagesUpdateMode mode : values()) {
      if (mode.getName().equals(text)) return mode;
    }
    return null;
  }
}
