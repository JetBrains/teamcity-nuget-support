/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
 * Created 18.03.13 12:22
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public enum PackagesPackDirectoryMode {
  LEAVE_AS_IS("as_is", "Do not specify"),
  EXPLICIT_DIRECTORY("explicit", "Use explicit directory"),
  PROJECT_DIRECTORY("project", "Use Project/.nuspec directory"),
  ;

  private final String myValue;
  private final String myDescription;

  private PackagesPackDirectoryMode(String value, String description) {
    myValue = value;
    myDescription = description;
  }

  @NotNull
  public String getValue() {
    return myValue;
  }

  @NotNull
  public String getDescription() {
    return myDescription;
  }

  public boolean isShowBaseDirectorySelector() {
    return this == EXPLICIT_DIRECTORY;
  }

  @NotNull
  public static PackagesPackDirectoryMode fromString(@Nullable final String value) {
    if (StringUtil.isEmptyOrSpaces(value)) {
      ///this is compatibility
      return EXPLICIT_DIRECTORY;
    }
    for (PackagesPackDirectoryMode mode : values()) {
      if (mode.myValue.equals(value)) return mode;
    }
    return LEAVE_AS_IS;
  }

}
