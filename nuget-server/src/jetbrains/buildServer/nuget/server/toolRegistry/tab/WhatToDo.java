package jetbrains.buildServer.nuget.server.toolRegistry.tab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 27.12.12 18:25
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public enum WhatToDo {
  INSTALL("install"),
  UPLOAD("custom"),
  REMOVE("remove"),
  DEFAULT("default"),
  ;
  private final String myName;

  private WhatToDo(String name) {
    myName = name;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Nullable
  public static WhatToDo fromString(@Nullable String s) {
    for (WhatToDo whatToDo : values()) {
      if (whatToDo.getName().equals(s)) return whatToDo;
    }
    return null;
  }
}

