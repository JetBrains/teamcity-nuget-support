/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.impl;

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
