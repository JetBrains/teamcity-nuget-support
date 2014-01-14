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

package jetbrains.buildServer.nuget.server.trigger.impl.checker;

import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestMode;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestModeNuGet;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 20:15
 */
public class PackageCheckerNuGetBase {
  public boolean accept(@NotNull PackageCheckRequest request) {
    return (request.getMode() instanceof CheckRequestModeNuGet);
  }

  @NotNull
  protected File getNuGetPath(@NotNull CheckRequestMode entry) {
    return ((CheckRequestModeNuGet)entry).getNuGetPath();
  }
}
