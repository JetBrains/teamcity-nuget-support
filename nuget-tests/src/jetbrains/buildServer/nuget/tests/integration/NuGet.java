/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import com.intellij.execution.configurations.GeneralCommandLine;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 31.08.11 10:39
*/
public enum NuGet {
  NuGet_1_6(1,6),
  NuGet_1_7(1,7),
  NuGet_1_8(1,8),
  NuGet_2_0(2,0),
  NuGet_2_1(2,1),
  NuGet_2_2(2,2),
  NuGet_2_5(2,5),
  NuGet_2_6(2,6),
  NuGet_2_7(2,7),
  NuGet_2_8(2,8),
  NuGet_3_2(3,2),
  NuGet_3_3(3,3),
  NuGet_3_4(3,4),
  NuGet_3_6(3,5);

  public final int major;
  public final int minor;

  NuGet(int major, int minor) {
    this.major = major;
    this.minor = minor;
  }

  @NotNull
  public File getPath() {
    switch (this) {
      case NuGet_1_6:
        return Paths.getPackagesPath("NuGet.CommandLine.1.6.0/tools/NuGet.exe");
      case NuGet_1_7:
        return Paths.getPackagesPath("NuGet.CommandLine.1.7.0/tools/NuGet.exe");
      case NuGet_1_8:
        return Paths.getPackagesPath("NuGet.CommandLine.1.8.0/tools/NuGet.exe");
      case NuGet_2_0:
        return Paths.getPackagesPath("NuGet.CommandLine.2.0.0/tools/NuGet.exe");
      case NuGet_2_1:
        return Paths.getPackagesPath("NuGet.CommandLine.2.1.0/tools/NuGet.exe");
      case NuGet_2_2:
        return Paths.getPackagesPath("NuGet.CommandLine.2.2.0/tools/NuGet.exe");
      case NuGet_2_5:
        return Paths.getPackagesPath("NuGet.CommandLine.2.5.0/tools/NuGet.exe");
      case NuGet_2_6:
        return Paths.getPackagesPath("NuGet.CommandLine.2.6.0/tools/NuGet.exe");
      case NuGet_2_7:
        return Paths.getPackagesPath("NuGet.CommandLine.2.7.0/tools/NuGet.exe");
      case NuGet_2_8:
        return Paths.getPackagesPath("NuGet.CommandLine.2.8.0/tools/NuGet.exe");
      case NuGet_3_2:
        return Paths.getPackagesPath("NuGet.CommandLine.3.2.0/tools/nuget.exe");
      case NuGet_3_3:
        return Paths.getPackagesPath("NuGet.CommandLine.3.3.0/tools/NuGet.exe");
      case NuGet_3_4:
        return Paths.getPackagesPath("NuGet.CommandLine.3.4.3/tools/NuGet.exe");
      case NuGet_3_6:
        return Paths.getPackagesPath("NuGet.CommandLine.3.6.0-rc-1974/tools/nuget.exe");
      default:
        throw new IllegalArgumentException("Failed to find nuget " + this);
    }
  }

  public void makeOutputVerbose(GeneralCommandLine cmd){
    if(this.major >= 2 && this.minor > 0){
      cmd.addParameter("-Verbosity");
      cmd.addParameter("detailed");
    } else{
      cmd.addParameter("-Verbose");
    }
  }
}
