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

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:44
 */
public class Paths {
  @NotNull
  public static File getTestDataPath() {
    return FileUtil.getCanonicalFile(new File("testData"));
  }

  @NotNull
  public static File getTestDataPath(@NotNull final String p) {
    return FileUtil.getCanonicalFile(new File(getTestDataPath(), p));
  }

  @NotNull
  public static File getPackagesPath(@NotNull final String p) {
    return FileUtil.getCanonicalFile(new File("../nuget-extensions/packages", p));
  }

  @NotNull
  public static File getNuGetRunnerPath() {
    return FileUtil.getCanonicalFile(new File("../nuget-extensions/bin/JetBrains.TeamCity.NuGetRunner.exe"));
  }

  @NotNull
  public static File getCredentialProviderHomeDirectory() {
    return FileUtil.getCanonicalFile(new File("../nuget-extensions/bin/credential-provider"));
  }
}
