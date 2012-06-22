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

package jetbrains.buildServer.nuget.agent.commands.impl;

import jetbrains.buildServer.nuget.agent.commands.NuGetVersion;
import jetbrains.buildServer.nuget.agent.commands.NuGetVersionCallback;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 19:56
 */
public class NuGetVersionHolderImpl implements NuGetVerisonHolder, NuGetVersionCallback {
  private final AtomicReference<NuGetVersion> myVersion = new AtomicReference<NuGetVersion>();
  @NotNull
  public NuGetVersion getNuGetVerion() {
    return myVersion.get();
  }

  public void onNuGetVersionCompleted(@NotNull NuGetVersion version) {
    myVersion.set(version);
  }
}
