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

package jetbrains.buildServer.nuget.common;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 13.08.13 13:48
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public enum PackagesInstallMode {
  /**
   * This method calls one NuGet command to update all packages
   */
  VIA_RESTORE("restore"),

  /**
   * This methos must call a NuGet udpate command per packages.config
   */
  VIA_INSTALL("install"),
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
