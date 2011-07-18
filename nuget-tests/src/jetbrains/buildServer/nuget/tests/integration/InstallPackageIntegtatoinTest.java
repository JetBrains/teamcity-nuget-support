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

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.nuget.agent.install.PackageUsages;
import jetbrains.buildServer.nuget.agent.install.PackagesInstallerRunner;
import jetbrains.buildServer.nuget.agent.install.impl.NuGetActionFactoryImpl;
import jetbrains.buildServer.nuget.agent.parameters.NuGetParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import jetbrains.buildServer.util.ArchiveUtil;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 08.07.11 2:15
 */
public class InstallPackageIntegtatoinTest extends BuildProcessTestCase {
  private File myRoot;
  private Mockery m;
  private AgentRunningBuild myBuild;
  private BuildRunnerContext myContext;
  private BuildProgressLogger myLogger;
  private PackagesParametersFactory myParametersFactory;
  private PackagesInstallParameters myInstall;
  private PackagesUpdateParameters myUpdate;
  private NuGetParameters myNuGet;
  private BuildProcess myMockProcess;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myRoot = createTempDir();
    m = new Mockery();
    myBuild = m.mock(AgentRunningBuild.class);
    myContext = m.mock(BuildRunnerContext.class);
    myLogger = m.mock(BuildProgressLogger.class);
    myParametersFactory = m.mock(PackagesParametersFactory.class);
    myInstall = m.mock(PackagesInstallParameters.class);
    myUpdate = m.mock(PackagesUpdateParameters.class);
    myMockProcess = m.mock(BuildProcess.class);
    myNuGet = m.mock(NuGetParameters.class);

    m.checking(new Expectations() {{
      allowing(myContext).getBuild();  will(returnValue(myBuild));
      allowing(myBuild).getBuildLogger(); will(returnValue(myLogger));
      allowing(myBuild).getCheckoutDirectory(); will(returnValue(myRoot));

      allowing(myMockProcess).start();
      allowing(myMockProcess).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      allowing(myLogger).message(with(any(String.class)));
      allowing(myLogger).activityStarted(with(equal("install")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("install")), with(any(String.class)));

      allowing(myInstall).getNuGetParameters(); will(returnValue(myNuGet));
      allowing(myUpdate).getNuGetParameters(); will(returnValue(myNuGet));
    }});
  }

  @Test
  public void test_01_online_sources() throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath(), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), false, false);

    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(4, packageses.size());
  }

  @NotNull
  private File getTestDataPath() {
    return Paths.getTestDataPath("integration/test-01.zip");
  }

  @Test
  public void test_01_online_sources_update() throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath(), "", myRoot);

    m.checking(new Expectations(){{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdate).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdate).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdate).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG));
    }});

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), false, true);


    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.10.11092").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(5, packageses.size());
  }

  @Test
  public void test_01_online_sources_update_forSln() throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath(), "", myRoot);

    m.checking(new Expectations(){{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdate).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdate).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdate).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_SLN));
    }});

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), false, true);


    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.10.11092").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(5, packageses.size());
  }

  @Test
  public void test_01_online_sources_update_safe() throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath(), "", myRoot);

    m.checking(new Expectations(){{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdate).getUseSafeUpdate(); will(returnValue(true));
      allowing(myUpdate).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdate).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG));
    }});

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), false, true);


    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.10.11092").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(5, packageses.size());
  }

  @Test
  public void test_01_online_sources_ecludeVersion() throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath(), "", myRoot);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Collections.<String>emptyList(), true, false);

    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications").isDirectory());
    Assert.assertEquals(4, packageses.size());
  }

  @Test(enabled = false, dependsOnGroups = "Need to understand how to check NuGet uses only specified sources")
  public void test_01_local_sources() throws RunBuildException {
    ArchiveUtil.unpackZip(getTestDataPath(), "", myRoot);
    File sourcesDir = new File(myRoot, "js");
    ArchiveUtil.unpackZip(Paths.getTestDataPath("test-01-sources.zip"), "", sourcesDir);

    fetchPackages(new File(myRoot, "sln1-lib.sln"), Arrays.asList("file:///" + sourcesDir.getPath()), false, false);

    List<File> packageses = Arrays.asList(new File(myRoot, "packages").listFiles());
    System.out.println("installed packageses = " + packageses);

    Assert.assertTrue(new File(myRoot, "packages/NUnit.2.5.7.10213").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/NInject.2.2.1.4").isDirectory());
    Assert.assertTrue(new File(myRoot, "packages/Machine.Specifications.0.4.13.0").isDirectory());
    Assert.assertEquals(4, packageses.size());
  }

  private void fetchPackages(final File sln,
                             final List<String> sources,
                             final boolean excludeVersion,
                             final boolean update) throws RunBuildException {
    final PackageUsages pu = m.mock(PackageUsages.class);
    final BuildProcess pup = m.mock(BuildProcess.class, "report usages");
    m.checking(new Expectations() {{
      allowing(myParametersFactory).loadNuGetParameters(myContext);  will(returnValue(myNuGet));
      allowing(myParametersFactory).loadInstallPackagesParameters(myContext, myNuGet);  will(returnValue(myInstall));

      allowing(myNuGet).getNuGetExeFile();
      will(returnValue(Paths.getPathToNuGet()));
      allowing(myNuGet).getSolutionFile(); will(returnValue(sln));
      allowing(myNuGet).getNuGetPackageSources(); will(returnValue(sources));
      allowing(myInstall).getExcludeVersion(); will(returnValue(excludeVersion));
      allowing(myParametersFactory).loadUpdatePackagesParameters(myContext, myNuGet);  will(returnValue(update ? myUpdate : null));

      allowing(pu).createReport(with(any(File.class)));
      will(returnValue(pup));

      allowing(pup).start();
      allowing(pup).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));
    }});

    BuildProcess proc = new PackagesInstallerRunner(
            new NuGetActionFactoryImpl(executingFactory(), pu),
            myParametersFactory
    ).createBuildProcess(myBuild, myContext);

    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  private CommandlineBuildProcessFactory executingFactory() {
    return new CommandlineBuildProcessFactory() {
      public BuildProcess executeCommandLine(@NotNull final BuildRunnerContext hostContext,
                                             @NotNull final File program,
                                             @NotNull final Collection<String> argz,
                                             @NotNull final File workingDir) throws RunBuildException {
        return new BuildProcessBase() {
          @NotNull
          @Override
          protected BuildFinishedStatus waitForImpl() throws RunBuildException {
            GeneralCommandLine cmd = new GeneralCommandLine();
            cmd.setExePath(program.getPath());
            for (String arg : argz) {
              cmd.addParameter(arg);
            }
            cmd.setWorkingDirectory(workingDir);

            System.out.println("Run: " + cmd.getCommandLineString());

            ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);

            System.out.println(result.getStdout());
            System.out.println(result.getStderr());

            return result.getExitCode() == 0
                    ? BuildFinishedStatus.FINISHED_SUCCESS
                    : BuildFinishedStatus.FINISHED_FAILED;
          }
        };
      }
    };
  }
}
