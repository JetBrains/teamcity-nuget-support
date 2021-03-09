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

import jetbrains.buildServer.util.FileUtil;
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
    return getCanonicalFile("bin/JetBrains.TeamCity.NuGetRunner.exe");
  }

  @NotNull
  @Override
  public String getPluginCorePath(int minSdkVersion) {
    if(minSdkVersion <= 3) {
      return getCanonicalFile("/bin/credential-plugin/netcoreapp" + minSdkVersion + ".0/CredentialProvider.TeamCity.dll").getPath();
    }

    return getCanonicalFile("/bin/credential-plugin/net" + minSdkVersion + ".0/CredentialProvider.TeamCity.dll").getPath();
  }

  @NotNull
  @Override
  public String getPluginFxPath() {
    return getCanonicalFile("/bin/credential-plugin/net46/CredentialProvider.TeamCity.exe").getPath();
  }

  @NotNull
  public File getCredentialProviderHomeDirectory() {
    return getCanonicalFile("bin/credential-provider");
  }

  private File getCanonicalFile(final String path) {
    return FileUtil.getCanonicalFile(new File(myNugetBinariesRoot, path));
  }
}
