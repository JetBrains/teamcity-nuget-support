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
