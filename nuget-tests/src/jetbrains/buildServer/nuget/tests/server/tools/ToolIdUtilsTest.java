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

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.tool.ToolIdUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

import static jetbrains.buildServer.nuget.common.FeedConstants.NUGET_EXTENSION;

/**
 * @author Evgeniy.Koshkin
 */
public class ToolIdUtilsTest extends BaseTestCase {

  @DataProvider
  public Object[][] packageNames() {
    return new Object[][]{
      {"NuGet.CommandLine.1.2.3", "1.2.3"},
      {"NuGet.CommandLine.2.3.4.5", "2.3.4.5", },
      {"NuGet.CommandLine.2.3.4.5-alpha", "2.3.4.5-alpha"},
      {"win-x86-commandline.3.2.0", "win-x86-commandline.3.2.0"},
      {"win-x86-commandline.3.2.0-rc", "win-x86-commandline.3.2.0-rc"},
      {"NuGet.Hack.2.3.4.5-alpha", "NuGet.Hack.2.3.4.5-alpha"},
      {"nuget.commandline.1.2.3", "1.2.3"},
      {"NUGET.COMMANDLINE.1.2.3", "1.2.3"},
      {"NuGet.CommandLine.4.8.0-rtm.5369+9f75ce12e9d1e153c7a087f6d01cc09861effe26", "4.8.0-rtm.5369"},
    };
  }

  @Test(dataProvider = "packageNames")
  public void testGetPackageVersion(final String packageName, final String expectedName) throws Exception {
    assertEquals(expectedName, ToolIdUtils.getPackageVersion(packageOfName(packageName)));
  }

  private File packageOfName(String packageName) {
    return new File(packageName + NUGET_EXTENSION);
  }
}
