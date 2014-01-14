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

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestMode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 13:56
 */
public class PackageCheckRequest {
  @NotNull private final CheckRequestMode myMode; //way to check version, i.e. java base, nuget base
  @NotNull private final SourcePackageReference myPackage;

  private long myCheckInterval = 5 * 60 * 1000; //5min is default, see jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckerSettings

  public PackageCheckRequest(@NotNull final CheckRequestMode mode,
                             @NotNull final SourcePackageReference aPackage) {
    myMode = mode;
    myPackage = aPackage;
  }

  /**
   * @return updates check interval in milliseconds
   */
  public long getCheckInterval() {
    return myCheckInterval;
  }

  /**
   * Sets update check interval
   * @param checkInterval in milliseconds
   */
  public void setCheckInterval(long checkInterval) {
    myCheckInterval = checkInterval;
  }

  @NotNull
  public CheckRequestMode getMode() {
    return myMode;
  }

  @NotNull
  public SourcePackageReference getPackage() {
    return myPackage;
  }
}
