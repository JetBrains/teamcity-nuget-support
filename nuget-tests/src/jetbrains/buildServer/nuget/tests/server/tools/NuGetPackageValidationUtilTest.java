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
import jetbrains.buildServer.nuget.server.tool.impl.NuGetPackageValidationUtil;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.util.FileUtil;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackageValidationUtilTest extends BaseTestCase {
  @Test
  public void testPackageValidationOldPackage() throws ToolException, IOException {
    File testPackage = createTempFile();
    FileUtil.copy(Paths.getPackagesPath("NuGet.CommandLine.1.8.0/NuGet.CommandLine.1.8.0.nupkg"), testPackage);
    NuGetPackageValidationUtil.validatePackage(testPackage);
  }

  @Test
  public void testPackageValidation() throws ToolException, IOException {
    File testPackage = createTempFile();
    FileUtil.copy(Paths.getPackagesPath("NuGet.CommandLine.3.4.4-rtm-final/NuGet.CommandLine.3.4.4-rtm-final.nupkg"), testPackage);
    NuGetPackageValidationUtil.validatePackage(testPackage);
  }

  @Test(expectedExceptions = ToolException.class)
  public void testPackageValidataionFailed() throws IOException, ToolException {
    NuGetPackageValidationUtil.validatePackage(createTempFile(22233));
  }
}
