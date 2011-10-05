/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.trigger.impl;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 16:45
 */
public class CheckRequestModeNuGet implements CheckRequestMode {
  private final File myNuGetPath;

  public CheckRequestModeNuGet(File nuGetPath) {
    myNuGetPath = nuGetPath;
  }

  @NotNull
  public File getNuGetPath() {
    return myNuGetPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CheckRequestModeNuGet that = (CheckRequestModeNuGet) o;
    return myNuGetPath.equals(that.myNuGetPath);
  }

  @Override
  public int hashCode() {
    return myNuGetPath.hashCode();
  }

  @Override
  public String toString() {
    return "CheckRequestModeNuGet{" +
            "myNuGetPath=" + myNuGetPath +
            '}';
  }
}
