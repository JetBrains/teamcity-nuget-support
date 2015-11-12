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

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.InstalledNuGetAsExecutable;
import org.testng.annotations.Test;

/**
 * @author Evgeniy.Koshkin
 */
public class InstalledNuGetAsExecutableTest extends BaseTestCase {
  @Test
  public void testGetVersionFromFileName() throws Exception {
    assertEquals("1.2.3", InstalledNuGetAsExecutable.getVersionFromFileName("NuGet.CommandLine.1.2.3.exe"));
    assertEquals("2.3.4.5", InstalledNuGetAsExecutable.getVersionFromFileName("NuGet.CommandLine.2.3.4.5.exe"));
    assertEquals("2.3.4.5-alpha", InstalledNuGetAsExecutable.getVersionFromFileName("NuGet.CommandLine.2.3.4.5-alpha.exe"));
    assertEquals("win-x86-commandline.3.2.0", InstalledNuGetAsExecutable.getVersionFromFileName("win-x86-commandline.3.2.0.exe"));
    assertEquals("win-x86-commandline.3.2.0-rc", InstalledNuGetAsExecutable.getVersionFromFileName("win-x86-commandline.3.2.0-rc.exe"));
  }
}
