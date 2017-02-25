/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.common.exec;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created 04.01.13 16:22
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetTeamCityProviderBase implements NuGetTeamCityProvider {
  @NotNull
  private final File myNugetBinariesRoot;

  public NuGetTeamCityProviderBase(@NotNull final File nugetBinariesRoot) {
    myNugetBinariesRoot = nugetBinariesRoot;
  }

  @NotNull
  public final File getNuGetRunnerPath() {
    return new File(myNugetBinariesRoot, "bin/JetBrains.TeamCity.NuGetRunner.exe");
  }

  @NotNull
  public File getCredentialProviderHomeDirectory() {
    return new File(myNugetBinariesRoot, "bin/credential-provider");
  }
}
