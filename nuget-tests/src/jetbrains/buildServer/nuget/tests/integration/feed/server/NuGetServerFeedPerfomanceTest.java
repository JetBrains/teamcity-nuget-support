/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.FileUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 03.11.11 17:02
 */
public class NuGetServerFeedPerfomanceTest extends NuGetServerFeedIntegrationTestBase {

  @Test
  public void test_list_5000_packages() throws IOException, PackageLoadException {
    doPackagesListTest(5000, 14.0);
  }

  @Test
  public void test_list_1000_packages() throws IOException, PackageLoadException {
    doPackagesListTest(1000, 6.0);
  }

  @Test
  public void test_list_100_packages() throws IOException, PackageLoadException {
    doPackagesListTest(100, 4.0);
  }

  @Test
  public void test_list_named_1000_packages() throws IOException, PackageLoadException {
    doPackagesListNamedTest(1000, 6.0);
  }

  @Test
  public void test_install_1000_packages() throws IOException, PackageLoadException {
    doPackagesListNamedTest(1000, 6.0);
  }

  private void doPackagesListTest(int packages, double time) throws IOException, PackageLoadException {
    enableDebug();

    setLongResponse(packages);

    final Runnable listCommand = new Runnable() {
      public void run() {
        final GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setExePath(NuGet.NuGet_1_5.getPath().getPath());
        cmd.addParameter("list");
        cmd.addParameter("-Source");
        cmd.addParameter(myNuGetServerUrl);
        cmd.addParameter("-AllVersions");

        try {
          final Process process = cmd.createProcess();
          FileUtil.closeAll(process.getInputStream(), process.getOutputStream(), process.getErrorStream());
          Assert.assertEquals(process.waitFor(), 0);
        } catch (Exception e) {
          Assert.fail();
        }
      }
    };

    assertTime(time, "Check nuget list command", 5, listCommand);
  }

  private void doPackagesListNamedTest(int packages, double time) throws IOException, PackageLoadException {
    enableDebug();

    setLongResponse(packages);

    final Runnable listCommand = new Runnable() {
      public void run() {
        final GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setExePath(NuGet.NuGet_1_5.getPath().getPath());
        cmd.addParameter("list");
        cmd.addParameter("CommonService5.Locator32");
        cmd.addParameter("-Source");
        cmd.addParameter(myNuGetServerUrl);
        cmd.addParameter("-AllVersions");

        try {
          final Process process = cmd.createProcess();
          FileUtil.closeAll(process.getInputStream(), process.getOutputStream(), process.getErrorStream());
          Assert.assertEquals(process.waitFor(), 0);
        } catch (Exception e) {
          Assert.fail();
        }
      }
    };

    assertTime(time, "Check nuget list command", 5, listCommand);
  }


  private void doPackagesInstallTest(int packages, double time) throws IOException, PackageLoadException {
    enableDebug();

    setLongResponse(packages);

    final Runnable listCommand = new Runnable() {
      public void run() {
        final GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setExePath(NuGet.NuGet_1_5.getPath().getPath());
        cmd.addParameter("install");
        cmd.addParameter("CommonService5.Locator32");
        cmd.addParameter("-Source");
        cmd.addParameter(myNuGetServerUrl);

        try {
          final Process process = cmd.createProcess();
          FileUtil.closeAll(process.getInputStream(), process.getOutputStream(), process.getErrorStream());
          process.waitFor();
        } catch (Exception e) {
          Assert.fail();
        }
      }
    };

    assertTime(time, "Check nuget list command", 5, listCommand);
  }

  private void setLongResponse(int packages) throws IOException, PackageLoadException {
    final String packageId = "CommonServiceLocator";
    final File responseFile = createTempFile();

    final String name = packageId + ".1.0.nupkg";
    renderPackagesResponseFile(responseFile, Paths.getTestDataPath("/packages/" + name));

    //sample package info:
    //##teamcity[package Id='CommonServiceLocator' Version='1.0' IsLatestVersion='true' teamcity.artifactPath='some/package/download/CommonServiceLocator.1.0.nupkg' Authors='Microsoft' Description='The Common Service Locator library contains a shared interface for service location which application and framework developers can reference. The library provides an abstraction over IoC containers and service locators. Using the library allows an application to indirectly access the capabilities without relying on hard references. The hope is that using this library, third-party applications and frameworks can begin to leverage IoC/Service Location without tying themselves down to a specific implementation.'  LastUpdated='2011-10-21T16:34:09Z' LicenseUrl='http://commonservicelocator.codeplex.com/license' PackageHash='RJjv0yxm+Fk/ak/CVMTGr0ng7g/nudkVYos4eQrIDpth3BdE1j7J2ddRm8FXtOoIZbgDqTU6hKq5zoackwL3HQ==' PackageHashAlgorithm='SHA512' PackageSize='37216' ProjectUrl='http://commonservicelocator.codeplex.com/' RequireLicenseAcceptance='false' TeamCityBuildId='42' TeamCityDownloadUrl='/app/nuget-packages/jonnyzzz5Z8mBocMOdtH4CJhxRaev11WxcWpHVCrrulezz/42/some/package/download/CommonServiceLocator.1.0.nupkg']

    final String serviceMessage = loadAllText(responseFile);
    Writer w = new FileWriter(responseFile);

    for(int i =0; i < packages; i++) {
      w.append(
              serviceMessage
                      .replaceAll("Id='[^']+'", "Id='CommonService" + i + ".Locator32'")
                      .replaceAll("Version='1.0'", "Version='1." + i + ".0.3'")
                      .replaceAll("IsLatestVersion='[^']+'", "IsLatestVersion='" + (i == 0) + "'")
      );
      w.append("   \n");
    }
    FileUtil.close(w);

    registerHttpHandler(packagesFileHandler(responseFile));
  }
}
