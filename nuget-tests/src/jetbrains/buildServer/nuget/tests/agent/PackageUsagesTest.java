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

package jetbrains.buildServer.nuget.tests.agent;

import com.intellij.util.Function;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorImpl;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesConfigParser;
import jetbrains.buildServer.nuget.agent.dependencies.impl.PackageUsagesImpl;
import jetbrains.buildServer.nuget.common.NuGetPackageInfo;
import jetbrains.buildServer.nuget.common.PackageDependencies;
import jetbrains.buildServer.nuget.common.PackageInfoLoader;
import jetbrains.buildServer.nuget.common.SourcePackageInfo;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.StringUtil;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 17:49
 */
public class PackageUsagesTest extends BaseTestCase {
  private NuGetPackagesCollector myCollector;
  private PackageUsages myUsages;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCollector = new NuGetPackagesCollectorImpl();
    myUsages = new PackageUsagesImpl(myCollector, new NuGetPackagesConfigParser(), new PackageInfoLoader());
  }

  @Test
  public void testLoadNuGetConfig_1() {
    final File config = Paths.getTestDataPath("config/test-01.xml");
    myUsages.reportInstalledPackages(config);

    assertCollected(t("elmah@1.1"), t());
  }

  @Test
  public void testLoadNuGetConfig_2() {
    final File config = Paths.getTestDataPath("config/test-02.xml");
    myUsages.reportInstalledPackages(config);

    assertCollected(t("elmah@1.1"), t());
  }

  @Test
  public void testLoadNuGetConfig_3() {
    final File config = Paths.getTestDataPath("config/test-03.xml");
    myUsages.reportInstalledPackages(config);

    assertCollected(t("Machine.Specifications@0.4.13.0", "NUnit@2.5.7.10213"), t());
  }

  @Test
  public void testLoadNuGetConfig_4() {
    final File config = Paths.getTestDataPath("config/test-04.xml");
    myUsages.reportInstalledPackages(config);

    assertCollected(t("EasyHttp@1.0.6", "JsonFx@2.0.1106.2610", "structuremap@2.6.2"), t());
  }

  @Test
  public void testCreatedPackages_empty() {
    myUsages.reportCreatedPackages(Collections.<File>emptyList());
    assertCollected(t(), t());
  }

  @Test
  public void testCreatedPackages_invalid() throws IOException {
    myUsages.reportCreatedPackages(Arrays.asList(createTempFile("msdmkflnasdf")));
    assertCollected(t(), t());
  }

  @Test
  public void testCreatedPackages_not_exist() throws IOException {
    myUsages.reportCreatedPackages(Arrays.asList(new File("wrong file")));
    assertCollected(t(), t());
  }

  @Test
  public void testCreatedPackages_package_1() throws IOException {
    myUsages.reportCreatedPackages(Arrays.asList(Paths.getTestDataPath("packages/CommonServiceLocator.1.0.nupkg")));
    assertCollected(t(), t("CommonServiceLocator@1.0"));
  }

  @Test
  public void testCreatedPackages_package_2() throws IOException {
    myUsages.reportCreatedPackages(Arrays.asList(Paths.getTestDataPath("packages/WebActivator.1.4.4.nupkg")));
    assertCollected(t(), t("WebActivator@1.4.4"));
  }

  @Test
  public void testCreatedPackages_packages() throws IOException {
    myUsages.reportCreatedPackages(Arrays.asList(
            Paths.getTestDataPath("packages/WebActivator.1.4.4.nupkg"),
            Paths.getPackagesPath("NuGet.CommandLine.1.8.0/NuGet.CommandLine.1.8.0.nupkg"),
            Paths.getTestDataPath("packages/NuGet.Core.1.5.20902.9026.nupkg")
    ));
    assertCollected(t(), t("NuGet.CommandLine@1.8.0", "NuGet.Core@1.5.20902.9026", "WebActivator@1.4.4"));
  }

  @Test
  public void testPublishedPackages_packages() throws IOException {
    myUsages.reportPublishedPackage(Paths.getTestDataPath("packages/WebActivator.1.4.4.nupkg"), "aaa");
    myUsages.reportPublishedPackage(Paths.getPackagesPath("NuGet.CommandLine.1.8.0/NuGet.CommandLine.1.8.0.nupkg"), "bbb");
    myUsages.reportPublishedPackage(Paths.getTestDataPath("packages/NuGet.Core.1.5.20902.9026.nupkg"), null);

    assertCollected(t(), t(), t("NuGet.CommandLine@1.8.0@bbb", "NuGet.Core@1.5.20902.9026@null", "WebActivator@1.4.4@aaa"));
  }

  private void assertCollected(@NotNull Collection<String> used, @NotNull Collection<String> created) {
    assertCollected(used, created, t());
  }

  private void assertCollected(@NotNull Collection<String> used, @NotNull Collection<String> created, @NotNull Collection<String> published) {
    final PackageDependencies ps = myCollector.getUsedPackages();
    final String actualUsed = toString(ps.getUsedPackages());
    final String actualCreated = toString(ps.getCreatedPackages());
    final String actualPublished = toString3(ps.getPublishedPackages());

    final String expectedUsed = toString2(used);
    final String expectedCreated = toString2(created);
    final String expectedPublished = toString2(published);

    Assert.assertEquals(actualUsed, expectedUsed);
    Assert.assertEquals(actualCreated, expectedCreated);
    Assert.assertEquals(actualPublished, expectedPublished);
  }

  private String toString(Collection<NuGetPackageInfo> info) {
    return StringUtil.join(info, new Function<NuGetPackageInfo, String>() {
      public String fun(NuGetPackageInfo packageInfo) {
        return packageInfo.getId() + "@" + packageInfo.getVersion();
      }
    }, ", ");
  }

  private String toString2(Collection<String> info) {
    return StringUtil.join(info, ", ");
  }

  private String toString3(Collection<SourcePackageInfo> info) {
    return StringUtil.join(info, new Function<SourcePackageInfo, String>() {
      public String fun(SourcePackageInfo packageInfo) {
        return packageInfo.getPackageInfo().getId() + "@" + packageInfo.getPackageInfo().getVersion() + "@" + packageInfo.getSource();
      }
    }, ", ");
  }



  private static Collection<String> t(String... t) {
    return Arrays.asList(t);
  }
}
