

package jetbrains.buildServer.nuget.feed.server.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 17.01.12 17:29
*/
public enum ServerMode {
  Disabled("disabled"),
  Java("java"),
  ;

  private final String myKey;

  ServerMode(String key) {
    myKey = key;
  }

  @NotNull
  public String getValue() {
    return myKey;
  }

  @NotNull
  public static ServerMode parse(@Nullable String mode) {
    if (mode == null) return Disabled;
    //Convertion from older settings
    if ("true".equalsIgnoreCase(mode)) return Java;
    for (ServerMode v : values()) {
      if (v.getValue().equals(mode)) return v;
    }
    return Disabled;
  }
}
