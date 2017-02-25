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

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.TempFolderProvider;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.nuget.server.util.SystemInfoImpl;
import jetbrains.buildServer.util.Win32RegistryAccessorUnpacker;
import jetbrains.buildServer.util.Win32RegistryNativeAccessorImpl;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.11.11 15:28
 */
public class SystemInfoTest extends BaseTestCase {
  private SystemInfo myInfo;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final File tempFile = createTempDir();
    myInfo = new SystemInfoImpl(new Win32RegistryNativeAccessorImpl(new Win32RegistryAccessorUnpacker(new TempFolderProvider() {
          @NotNull
          public File getTempDirectory() {
            return tempFile;
          }
        })));
  }

  @Test
  public void testNet4IsDetected() throws IOException {
    if(!com.intellij.openapi.util.SystemInfo.isWindows) {
      return;
    }

    Assert.assertTrue(myInfo.isDotNetFrameworkAvailable());
  }

  @Test
  public void testNet4FlagCahced() throws IOException {
    if(!com.intellij.openapi.util.SystemInfo.isWindows) {
      return;
    }

    testNet4IsDetected();

    assertTime(0.1, "Check value is cached", new Runnable() {
      public void run() {
        for (int i = 0; i < 10000; i++) {
          myInfo.isDotNetFrameworkAvailable();
        }
      }
    });
  }

  @Test
  public void testWindowsDetected() {
    Assert.assertTrue(myInfo.canStartNuGetProcesses());
  }
}
