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

package jetbrains.buildServer.nuget.tests.server.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.edm.EdmSimpleType;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 07.01.12 9:48
*/
public final class Property {
  private final String myName;
  private final EdmSimpleType myType;
  private final String myAtomPath;

  public Property(@NotNull final String name, @NotNull final EdmSimpleType type, @Nullable final String atomPath) {
    myName = name;
    myType = type;
    myAtomPath = atomPath;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public EdmSimpleType getType() {
    return myType;
  }

  @Nullable
  public String getAtomPath() {
    return myAtomPath;
  }

  @Override
  public String toString() {
    return "Property{" +
            "myName='" + myName + '\'' +
            ", myType=" + myType +
            ", myAtomPath='" + myAtomPath + '\'' +
            '}';
  }
}
