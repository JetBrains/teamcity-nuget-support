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
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 13:56
 */
public class PackageCheckRequest {
  @NotNull private final CheckRequestMode myMode; //way to check version, i.e. java base, nuget base
  @Nullable private final String myPackageSource;
  @NotNull private final String myPacakgeId;
  @Nullable private final String myVersionSpec;

  private long myCheckInterval = 5 * 60 * 1000; //60s is default

  public PackageCheckRequest(@NotNull final CheckRequestMode mode,
                             @Nullable final String packageSource,
                             @NotNull final String pacakgeId,
                             @Nullable final String versionSpec) {
    myMode = mode;
    myPackageSource = packageSource;
    myPacakgeId = pacakgeId;
    myVersionSpec = versionSpec;
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

  @Nullable
  public String getPackageSource() {
    return myPackageSource;
  }

  @NotNull
  public String getPackageId() {
    return myPacakgeId;
  }

  @Nullable
  public String getVersionSpec() {
    return myVersionSpec;
  }
}
