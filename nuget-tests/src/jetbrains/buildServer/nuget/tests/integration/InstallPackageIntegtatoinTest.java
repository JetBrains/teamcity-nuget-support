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

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorImpl;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesInstallerRunner;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolverImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigProcessFactory;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesConfigScanner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.ResourcesConfigPackagesScanner;
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 08.07.11 2:15
 */
public class InstallPackageIntegtatoinTest extends IntegrationTestBase {
  protected PackagesInstallParameters myInstall;
  protected PackagesUpdateParameters myUpdate;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myInstall = m.mock(PackagesInstallParameters.class);
    myUpdate = m.mock(PackagesUpdateParameters.class);

    m.checking(new Expectations(){{
      allowing(myInstall).getNuGetParameters();
      will(returnValue(myNuGet));
      allowing(myUpdate).getNuGetParameters();
      will(returnValue(myNuGet));

      allowing(myLogger).activityStarted(with(equal("install")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("install")), with(any(String.class)));
    }});
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), false, false, nuget,
            Arrays.asList(
                    new PackageInfo("Machine.Specifications", "0.4.13.0"),
                    new PackageInfo("NUnit", "2.5.7.10213"),
                    new PackageInfo("Ninject", "2.2.1.4"))
    );

    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(4, packageses.size());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources_update_forConfig(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    m.checking(new Expectations() {{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdate).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdate).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdate).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG));
    }});

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), false, true, nuget, null);


    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.10.11092").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(5, packageses.size());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources_update_forSln(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    m.checking(new Expectations() {{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdate).getUseSafeUpdate();
      will(returnValue(false));
      allowing(myUpdate).getPackagesToUpdate();
      will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdate).getUpdateMode();
      will(returnValue(PackagesUpdateMode.FOR_SLN));
    }});

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), false, true, nuget, null);


    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.10.11092").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(5, packageses.size());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources_update_safe(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    m.checking(new Expectations() {{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdate).getUseSafeUpdate();
      will(returnValue(true));
      allowing(myUpdate).getPackagesToUpdate();
      will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdate).getUpdateMode();
      will(returnValue(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG));
    }});

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), false, true, nuget, null);


    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.10.11092").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(5, packageses.size());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources_ecludeVersion(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), true, false, nuget,
            Arrays.asList(
                    new PackageInfo("Machine.Specifications", "0.4.13.0"),
                    new PackageInfo("NUnit", "2.5.7.10213"),
                    new PackageInfo("Ninject", "2.2.1.4")));

    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications").isDirectory());
    Assert.assertEquals(4, packageses.size());
  }

  @Test(enabled = false, dependsOnGroups = "Need to understand how to check NuGet uses only specified sources", dataProvider = NUGET_VERSIONS)
  public void test_01_local_sources(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);
    File sourcesDir = new File(myRoot, "js");
    ArchiveUtil.unpackZip(Paths.getTestDataPath("test-01-sources.zip"), "", sourcesDir);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Arrays.asList("file:///" + sourcesDir.getPath()), false, false, nuget, null);

    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(4, packageses.size());
  }

  @Test(dataProvider = NUGET_VERSIONS_15p)
  public void test_02_NuGetConfig_anoterPackagesPath(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-02.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "ConsoleApplication1/ConsoleApplication1.sln"), Collections.<String>emptyList(), true, false, nuget,
            Arrays.asList(
                    new PackageInfo("Castle.Core", "3.0.0.3001"),
                    new PackageInfo("NUnit", "2.5.10.11092"),
                    new PackageInfo("jQuery", "1.7.1"),
                    new PackageInfo("Microsoft.Web.Infrastructure", "1.0.0.0"),
                    new PackageInfo("WebActivator", "1.5")));

    List<File> packageses = Arrays.asList(new File(myRoot, "lib").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "lib/NUnit").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/Castle.Core").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/jQuery").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/Microsoft.Web.Infrastructure").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/WebActivator").isDirectory());
    Assert.assertEquals(6, packageses.size());
  }

  @Test(dataProvider = NUGET_VERSIONS_15p, dependsOnGroups = "support no packages scenriod/parse sln/scan projects")
  public void test_no_packages_scenario(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("nuget-nopackages.zip"), "", myRoot);

    fetchPackages(
            new File(myRoot, "nuget-nopackages/ConsoleApplication1.sln"),
            Collections.<String>emptyList(), true, false, nuget,
            Arrays.asList(
                    new PackageInfo("Castle.Core", "3.0.0.3001"),
                    new PackageInfo("NUnit", "2.5.10.11092"),
                    new PackageInfo("jQuery", "1.7.1"),
                    new PackageInfo("Microsoft.Web.Infrastructure", "1.0.0.0"),
                    new PackageInfo("WebActivator", "1.5")));

    List<File> packageses = Arrays.asList(new File(myRoot, "lib").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "lib/NUnit").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/Castle.Core").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/jQuery").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/Microsoft.Web.Infrastructure").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/WebActivator").isDirectory());
    Assert.assertEquals(6, packageses.size());
  }

  @TestFor(issues = "TW-21061")
  @Test(dataProvider = NUGET_VERSIONS_17p)
  public void test_solution_wide_online_sources(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-shared-packages.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "ConsoleApplication1.sln"), Collections.<String>emptyList(), false, false, nuget,
            Arrays.asList(
                    new PackageInfo("Microsoft.Web.Infrastructure", "1.0.0.0"),
                    new PackageInfo("NUnit", "2.5.10.11092"),
                    new PackageInfo("Ninject", "3.0.0.15"),
                    new PackageInfo("WebActivator", "1.5"),
                    new PackageInfo("jQuery", "1.7.2"))
    );

    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/Microsoft.Web.Infrastructure.1.0.0.0").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.10.11092").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.3.0.0.15").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/WebActivator.1.5").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/jQuery.1.7.2").isDirectory());
    Assert.assertEquals(5 + 1 , packageses.size());
  }


  private void fetchPackages(final File sln,
                             final List<String> sources,
                             final boolean excludeVersion,
                             final boolean update,
                             @NotNull final NuGet nuget,
                             @Nullable Collection<PackageInfo> detectedPackages) throws RunBuildException {

    m.checking(new Expectations() {{
      allowing(myParametersFactory).loadNuGetFetchParameters(myContext);
      will(returnValue(myNuGet));
      allowing(myParametersFactory).loadInstallPackagesParameters(myContext, myNuGet);
      will(returnValue(myInstall));

      allowing(myNuGet).getNuGetExeFile();
      will(returnValue(nuget.getPath()));
      allowing(myNuGet).getSolutionFile();
      will(returnValue(sln));
      allowing(myNuGet).getNuGetPackageSources();
      will(returnValue(sources));
      allowing(myInstall).getExcludeVersion();
      will(returnValue(excludeVersion));
      allowing(myParametersFactory).loadUpdatePackagesParameters(myContext, myNuGet);
      will(returnValue(update ? myUpdate : null));
    }});

    BuildProcess proc = new PackagesInstallerRunner(
            myActionFactory,
            myParametersFactory,
            new LocateNuGetConfigProcessFactory(new RepositoryPathResolverImpl(), Arrays.<PackagesConfigScanner>asList(new ResourcesConfigPackagesScanner()))
    ).createBuildProcess(myBuild, myContext);
    ((NuGetPackagesCollectorImpl)myCollector).removeAllPackages();

    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);

    System.out.println(myCollector.getUsedPackages());
    if (detectedPackages != null) {
      Assert.assertEquals(
              new TreeSet<PackageInfo>(myCollector.getUsedPackages().getUsedPackages()),
              new TreeSet<PackageInfo>(detectedPackages));
    }

    m.assertIsSatisfied();
  }

}
