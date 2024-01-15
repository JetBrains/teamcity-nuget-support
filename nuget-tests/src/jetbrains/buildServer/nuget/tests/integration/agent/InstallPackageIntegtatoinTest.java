

package jetbrains.buildServer.nuget.tests.integration.agent;

import com.intellij.openapi.util.SystemInfo;
import java.util.Collection;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.TestNGUtil;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.nuget.common.NuGetPackageInfo;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.tests.agent.StartsWithMatcher;
import jetbrains.buildServer.nuget.tests.integration.MockNuGetAuthHTTP;
import jetbrains.buildServer.nuget.tests.integration.MockNuGetHTTP;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.TestFor;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 08.07.11 2:15
 */
public class InstallPackageIntegtatoinTest extends InstallPackageIntegrationTestCase {
  private MockNuGetHTTP myHttp;
  private List<String> mySource;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myHttp = new MockNuGetHTTP(NuGetAPIVersion.V2);
    myHttp.start();
    mySource = Collections.singletonList(myHttp.getSourceUrl());
    addGlobalSource(myHttp.getSourceUrl(), null, null);
    myHttp
      .withPackage("NUnit", "2.5.7.10213", "feed/mock/feed.nunit.2.5.7.package.xml", "feed/mock/feed.nunit.2.5.7.nupkg", false)
      .withPackage("NUnit", "2.5.10.11092", "feed/mock/feed.nunit.2.5.10.package.xml", "feed/mock/feed.nunit.2.5.10.nupkg", true)
      .withPackage("Ninject", "2.2.1.4", "feed/mock/feed.ninject.2.2.1.package.xml", "feed/mock/feed.ninject.2.2.1.nupkg", false)
      .withPackage("Ninject", "3.0.0.15", "feed/mock/feed.ninject.3.0.0.15.package.xml", "feed/mock/feed.ninject.3.0.0.15.nupkg", false)
      .withPackage("Ninject", "3.0.1.10", "feed/mock/feed.ninject.3.0.1.10.package.xml", "feed/mock/feed.ninject.3.0.1.10.nupkg", true)
      .withPackage("Machine.Specifications", "0.4.13.0", "feed/mock/feed.machine.0.4.13.package.xml", "feed/mock/feed.machine.0.4.13.nupkg", true)
      .withPackage("Castle.Core", "3.0.0.3001", "feed/mock/feed.castle.core.3.0.0.package.xml", "feed/mock/feed.castle.core.3.0.0.nupkg", true)
      .withPackage("jQuery", "1.7.1", "feed/mock/feed.jquery.1.7.1.package.xml", "feed/mock/feed.jquery.1.7.1.nupkg", false)
      .withPackage("jQuery", "1.7.2", "feed/mock/feed.jquery.1.7.2.package.xml", "feed/mock/feed.jquery.1.7.2.nupkg", true)
      .withPackage("Microsoft.Web.Infrastructure", "1.0.0", "feed/mock/feed.microsoft.web.infrastructure.1.0.0.package.xml", "feed/mock/feed.microsoft.web.infrastructure.1.0.0.nupkg", true)
      .withPackage("WebActivator", "1.5", "feed/mock/feed.webactivator.1.5.package.xml", "feed/mock/feed.webactivator.1.5.nupkg", true)
      .withPackage("elmah", "1.2.0", "feed/mock/feed.elmah.1.2.package.xml", "feed/mock/feed.elmah.1.2.nupkg", false)
      .withPackage("elmah", "1.2.2", "feed/mock/feed.elmah.1.2.2.package.xml", "feed/mock/feed.elmah.1.2.2.nupkg", true)
      .withPackage("elmah.corelibrary", "1.2.2", "feed/mock/feed.elmah.corelibrary.1.2.2.package.xml", "feed/mock/feed.elmah.corelibrary.1.2.2.nupkg", true);
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    myHttp.stop();
    super.tearDown();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213"),
                    new NuGetPackageInfo("Ninject", "2.2.1.4"))
    );

    String packages = "packages";
    List<File> packageses = listFiles(packages);
    System.out.println("installed packages = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Ninject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_28p)
  public void test_01_online_sources_restore(@NotNull final NuGet nuget) throws RunBuildException {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213"),
                    new NuGetPackageInfo("Ninject", "2.2.1.4"))
    );

    List<File> packages = listFiles("packages");
    System.out.println("installed packageses = " + packages);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Ninject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_17p)
  public void test_01_online_sources_no_cache(@NotNull final NuGet nuget) throws RunBuildException {
    if (!SystemInfo.isWindows && nuget.version.equals(NuGet.NuGet_3_3.version)) {
      TestNGUtil.skip("nuget 3.3 hangs on Linux");
    }

    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), false, true, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213"),
                    new NuGetPackageInfo("Ninject", "2.2.1.4"))
    );

    List<File> packages = listFiles("packages");
    System.out.println("installed packageses = " + packages);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Ninject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_28p)
  public void test_01_online_sources_no_cache_restore(@NotNull final NuGet nuget) throws RunBuildException {
    if (!SystemInfo.isWindows && nuget.version.equals(NuGet.NuGet_3_3.version)) {
      TestNGUtil.skip("nuget 3.3 hangs on Linux");
    }

    myInstallMode = PackagesInstallMode.VIA_RESTORE;
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), false, true, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213"),
                    new NuGetPackageInfo("Ninject", "2.2.1.4"))
    );

    List<File> packages = listFiles("packages");
    System.out.println("installed packageses = " + packages);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Ninject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources_update_forConfig(@NotNull final NuGet nuget) throws RunBuildException {
    if (!SystemInfo.isWindows && nuget.version.compareTo(NuGet.NuGet_4_8.version) < 0) {
      TestNGUtil.skip("nuget update command works only on Windows");
    }

    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);
    allowUpdate(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), false, false, true, nuget, null);

    List<File> packageses = listFiles("packages");
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Ninject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources_update_forSln(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);
    allowUpdate(PackagesUpdateMode.FOR_SLN);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), false, false, true, nuget, null);


    List<File> packageses = listFiles("packages");
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Ninject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_28p)
  public void test_01_online_sources_restore_update_forSln(@NotNull final NuGet nuget) throws RunBuildException {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);
    allowUpdate(PackagesUpdateMode.FOR_SLN);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), false, false, true, nuget, null);

    List<File> packageses = listFiles("packages");
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Ninject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources_update_safe(@NotNull final NuGet nuget) throws RunBuildException {
    if (!SystemInfo.isWindows && nuget.version.compareTo(NuGet.NuGet_4_8.version) < 0) {
      TestNGUtil.skip("nuget update command works only on Windows");
    }

    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);
    allowUpdate(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG, true, true);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), false, false, true, nuget, null);

    List<File> packageses = listFiles("packages");
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Ninject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_17p)
  public void test_prerelease_local(@NotNull final NuGet nuget) throws RunBuildException {
    if (!SystemInfo.isWindows && nuget.version.compareTo(NuGet.NuGet_4_8.version) < 0) {
      TestNGUtil.skip("nuget update command works only on Windows");
    }

    ArchiveUtil.unpackZip(getTestDataPath("test-prerelease.zip"), "prereleaseUpdate/", myRoot);
    allowUpdate(PackagesUpdateMode.FOR_SLN, true, false);

    clearGlobalSources();

    fetchPackages(new File(myRoot, "ClassLibrary1.sln"), Arrays.asList(new File(myRoot, "feed").getPath()), false, false, true, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Jonnyz.Package", "3.0.4001-beta"),
                    new NuGetPackageInfo("Elmah", "1.2")));

    List<File> packageses = listFiles("packages");
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/elmah.1.2").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Jonnyz.Package.3.0.3001").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Jonnyz.Package.3.0.4001-beta").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_17p)
  public void test_prerelease_config_removed(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-prerelease-cleanup.zip"), "prereleaseUpdate/", myRoot);

    m.checking(new Expectations() {{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdateParameters).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdateParameters).getIncludePrereleasePackages(); will(returnValue(true));

      allowing(myUpdateParameters).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdateParameters).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_SLN));

      allowing(myLogger).warning(with(new StartsWithMatcher("Packages.config file was removed by NuGet.exe update command")));
    }});

    fetchPackages(new File(myRoot, "ClassLibrary1.sln"), Arrays.asList(new File(myRoot, "feed").getPath()), false, false, true, nuget, null);
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_01_online_sources_ecludeVersion(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), true, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213"),
                    new NuGetPackageInfo("Ninject", "2.2.1.4")));

    List<File> packageses = listFiles("packages");
    System.out.println("installed packageses = " + packageses);

    final File packagesRootDir = new File(myRoot, "packages");
    assertDirectoryExist(packagesRootDir, "NUnit");
    assertDirectoryExist(packagesRootDir, "Ninject");
    assertDirectoryExist(packagesRootDir, "Machine.Specifications");
  }

  @Test(enabled = false, dataProvider = NUGET_VERSIONS)
  public void test_01_local_sources(@NotNull final NuGet nuget) throws RunBuildException {
    TestNGUtil.skip("Need to understand how to check NuGet uses only specified sources");
    ArchiveUtil.unpackZip(getTestDataPath("test-01.zip"), "", myRoot);
    File sourcesDir = new File(myRoot, "js");
    ArchiveUtil.unpackZip(Paths.getTestDataPath("test-01-sources.zip"), "", sourcesDir);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Arrays.asList("file:///" + sourcesDir.getPath()), false, false, false, nuget, null);

    List<File> packageses = listFiles("packages");
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Ninject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_17p)
  public void test_02_NuGetConfig_anoterPackagesPath(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-02.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "ConsoleApplication1/ConsoleApplication1.sln"), true, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Castle.Core", "3.0.0.3001"),
                    new NuGetPackageInfo("NUnit", "2.5.10.11092"),
                    new NuGetPackageInfo("jQuery", "1.7.1"),
                    new NuGetPackageInfo("Microsoft.Web.Infrastructure", "1.0.0.0"),
                    new NuGetPackageInfo("WebActivator", "1.5")));

    List<File> packageses = listFiles("lib");
    System.out.println("installed packageses = " + packageses);
    Assert.assertEquals(6, packageses.size());

    final File libDir = new File(myRoot, "lib");

    assertDirectoryExist(libDir, "NUnit");
    assertDirectoryExist(libDir, "Castle.Core");
    assertDirectoryExist(libDir, "jQuery");
    assertDirectoryExist(libDir, "Microsoft.Web.Infrastructure");
    assertDirectoryExist(libDir, "WebActivator");
  }

  @Test(dataProvider = NUGET_VERSIONS_15p)
  public void test_no_packages_scenario(@NotNull final NuGet nuget) throws RunBuildException {
    TestNGUtil.skip("support no packages scenriod/parse sln/scan projects");

    ArchiveUtil.unpackZip(getTestDataPath("nuget-nopackages.zip"), "", myRoot);

    fetchPackages(
            new File(myRoot, "nuget-nopackages/ConsoleApplication1.sln"),
            true, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Castle.Core", "3.0.0.3001"),
                    new NuGetPackageInfo("NUnit", "2.5.10.11092"),
                    new NuGetPackageInfo("jQuery", "1.7.1"),
                    new NuGetPackageInfo("Microsoft.Web.Infrastructure", "1.0.0.0"),
                    new NuGetPackageInfo("WebActivator", "1.5")));

    List<File> packageses = listFiles("lib");
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "lib/NUnit").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/Castle.Core").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/jQuery").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/Microsoft.Web.Infrastructure").isDirectory());
    Assert.assertTrue(new File(myRoot, "lib/WebActivator").isDirectory());
  }

  @TestFor(issues = "TW-21061")
  @Test(dataProvider = NUGET_VERSIONS_17p)
  public void test_solution_wide_online_sources(@NotNull final NuGet nuget) throws RunBuildException {
    if (!SystemInfo.isWindows && nuget.version.equals(NuGet.NuGet_3_3.version)) {
      TestNGUtil.skip("nuget 3.3 hangs on Linux");
    }

    ArchiveUtil.unpackZip(getTestDataPath("test-shared-packages.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "ConsoleApplication1.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Microsoft.Web.Infrastructure", "1.0.0.0"),
                    new NuGetPackageInfo("NUnit", "2.5.10.11092"),
                    new NuGetPackageInfo("Ninject", "3.0.0.15"),
                    new NuGetPackageInfo("WebActivator", "1.5"),
                    new NuGetPackageInfo("jQuery", "1.7.2"))
    );

    List<File> packageses = listFiles("packages");
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/Microsoft.Web.Infrastructure.1.0.0.0").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/WebActivator.1.5").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/jQuery.1.7.2").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_17p)
  public void test_solution_scanner(@NotNull final NuGet nuget) throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath("test-web-noRepository.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "ClassLibrary1/ClassLibrary1.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Ninject", "3.0.1.10"),
                    new NuGetPackageInfo("elmah", "1.2.2"),
                    new NuGetPackageInfo("elmah.corelibrary", "1.2.2")
                    )
    );

    List<File> packageses = listFiles("ClassLibrary1/packages");
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "ClassLibrary1/packages/Ninject.3.0.1.10").isDirectory());
    Assert.assertTrue(new File(myRoot, "ClassLibrary1/packages/elmah.1.2.2").isDirectory());
    Assert.assertTrue(new File(myRoot, "ClassLibrary1/packages/elmah.corelibrary.1.2.2").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_intall_mode_nuget_config_location_1(@NotNull final NuGet nuget) throws Exception {
    assertSame(PackagesInstallMode.VIA_INSTALL, myInstallMode);

    ArchiveUtil.unpackZip(getTestDataPath("test-nuget_config_location_1.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "Apps/FirstApp/FirstApp.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213")
                    ));

    List<File> packageses = listFiles("customizedPath");
    Assert.assertTrue(new File(myRoot, "customizedPath/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "customizedPath/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_install_mode_nuget_config_location_2(@NotNull final NuGet nuget) throws Exception {
    assertSame(PackagesInstallMode.VIA_INSTALL, myInstallMode);

    ArchiveUtil.unpackZip(getTestDataPath("test-nuget_config_location_2.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "App.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213")
            ));

    List<File> packageses = listFiles("customizedPath");
    Assert.assertTrue(new File(myRoot, "customizedPath/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "customizedPath/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_install_mode_nuget_config_location_3(@NotNull final NuGet nuget) throws Exception {
    assertSame(PackagesInstallMode.VIA_INSTALL, myInstallMode);

    ArchiveUtil.unpackZip(getTestDataPath("test-nuget_config_location_3.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "App.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213")
            ));

    List<File> packageses = listFiles("customizedPath");
    Assert.assertTrue(new File(myRoot, "customizedPath/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "customizedPath/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_28p)
  public void test_restore_mode_nuget_config_location_1(@NotNull final NuGet nuget) throws Exception {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;

    ArchiveUtil.unpackZip(getTestDataPath("test-nuget_config_location_1.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "Apps/FirstApp/FirstApp.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213")
            ));

    List<File> packageses = listFiles("customizedPath");
    Assert.assertTrue(new File(myRoot, "customizedPath/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "customizedPath/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_28p)
  public void test_restore_mode_nuget_config_location_2(@NotNull final NuGet nuget) throws Exception {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;

    ArchiveUtil.unpackZip(getTestDataPath("test-nuget_config_location_2.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "App.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213")
            ));

    List<File> packageses = listFiles("customizedPath");
    Assert.assertTrue(new File(myRoot, "customizedPath/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "customizedPath/Machine.Specifications.0.4.13.0").isDirectory());
  }

  @Test(dataProvider = NUGET_VERSIONS_28p)
  public void test_restore_mode_nuget_config_location_3(@NotNull final NuGet nuget) throws Exception {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;

    ArchiveUtil.unpackZip(getTestDataPath("test-nuget_config_location_3.zip"), "", myRoot);

    fetchPackages(new File(myRoot, "App.sln"), false, false, false, nuget,
            Arrays.asList(
                    new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
                    new NuGetPackageInfo("NUnit", "2.5.7.10213")
            ));

    List<File> packageses = listFiles("customizedPath");
    Assert.assertTrue(new File(myRoot, "customizedPath/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "customizedPath/Machine.Specifications.0.4.13.0").isDirectory());
  }

  protected void fetchPackages(final File sln,
                               final boolean excludeVersion,
                               final boolean noCache,
                               final boolean update,
                               @NotNull final NuGet nuget,
                               @Nullable Collection<NuGetPackageInfo> detectedPackages) throws RunBuildException {
    fetchPackages(sln, mySource, excludeVersion, noCache, update, nuget, detectedPackages);
  }

  private void assertDirectoryExist(File root, final String directoryNamePrefix) {
    assertNotNull("Direcotry not found by prefix " + directoryNamePrefix, CollectionsUtil.filterNulls(Arrays.asList(root.listFiles((FileFilter) new PrefixFileFilter(directoryNamePrefix)))));
  }

  @NotNull
  private List<File> listFiles(String dirRelativePath) {
    File dir = new File(myRoot, dirRelativePath);
    final File[] files = dir.listFiles();
    Assert.assertNotNull(files, "Failed to list for: " + dir);
    return Arrays.asList(files);
  }
}
