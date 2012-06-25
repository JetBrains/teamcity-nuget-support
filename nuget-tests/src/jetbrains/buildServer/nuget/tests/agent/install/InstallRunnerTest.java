/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.agent.install;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.NuGetVersion;
import jetbrains.buildServer.nuget.agent.commands.NuGetVersionCallback;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.impl.AuthStagesBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.InstallRunnerStagesBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.InstallStages;
import jetbrains.buildServer.nuget.agent.runner.install.impl.InstallStagesImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolverImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigProcessFactory;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesConfigScanner;
import jetbrains.buildServer.nuget.agent.util.CompositeBuildProcess;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.nuget.tests.agent.PackageSourceImpl;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 25.06.12 16:51
 */
public class InstallRunnerTest extends BuildProcessTestCase {
  private Mockery m;
  private NuGetActionFactory myActionFactory;
  private PackagesConfigScanner myScanner;
  private BuildRunnerContext myContext;
  private AgentRunningBuild myBuild;
  private BuildProgressLogger myLogger;
  private NuGetVersion myVersion;

  private NuGetFetchParameters myFetchParameters;
  private PackagesInstallParameters myInstallParameters;
  private PackagesUpdateParameters myUpgradeParameters;

  private InstallRunnerStagesBuilder myBuilder;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myActionFactory = m.mock(NuGetActionFactory.class);
    myScanner = m.mock(PackagesConfigScanner.class);
    myContext = m.mock(BuildRunnerContext.class);
    myBuild = m.mock(AgentRunningBuild.class);
    myLogger = m.mock(BuildProgressLogger.class);
    myVersion = m.mock(NuGetVersion.class);

    m.checking(new Expectations(){{
      allowing(myContext).getBuild(); will(returnValue(myBuild));
      allowing(myBuild).getBuildLogger(); will(returnValue(myLogger));

      allowing(myLogger).message(with(any(String.class)));
    }});

    myBuilder = new InstallRunnerStagesBuilder(
            new AuthStagesBuilder(myActionFactory),
            myActionFactory,
            new LocateNuGetConfigProcessFactory(
                    new RepositoryPathResolverImpl(),
                    Arrays.asList(myScanner)));

    myFetchParameters = m.mock(NuGetFetchParameters.class);
    myInstallParameters = m.mock(PackagesInstallParameters.class);
    myUpgradeParameters = m.mock(PackagesUpdateParameters.class);

    m.checking(new Expectations(){{
      oneOf(myActionFactory).createVersionCheckCommand(
              with(equal(myContext)),
              with(any(NuGetVersionCallback.class)),
              with(equal(myFetchParameters))
              );
      will(doAll(new CustomAction("callback") {
        public Object invoke(Invocation invocation) throws Throwable {
          ((NuGetVersionCallback)invocation.getParameter(1)).onNuGetVersionCompleted(myVersion);
          return createMockBuildProcess("version");
        }
      }));
    }});
  }

  @Test
  public void testInstallWithLogger() throws RunBuildException, IOException {
    CompositeBuildProcess bp = doBasicInstall(Collections.<PackageSource>emptyList(), false);
    assertRunSuccessfully(bp, BuildFinishedStatus.FINISHED_SUCCESS);

    assertExecutedMockProcesses("version", "install", "report");

    m.assertIsSatisfied();
  }

  @Test
  public void testInstallWithLogger_auth() throws RunBuildException, IOException {
    final List<PackageSource> sources = Arrays.<PackageSource>asList(new PackageSourceImpl("foo", "use", "pwd"));
    CompositeBuildProcess bp = doBasicInstall(sources, false);

    m.checking(new Expectations(){{
      allowing(myVersion).supportAuth(); will(returnValue(true));

      oneOf(myActionFactory).createAuthenticateFeeds(myContext, sources, myFetchParameters);
      will(returnValue(createMockBuildProcess("auth")));
    }});

    assertRunSuccessfully(bp, BuildFinishedStatus.FINISHED_SUCCESS);
    assertExecutedMockProcesses("version", "auth", "install", "report");

    m.assertIsSatisfied();
  }

  @Test
  public void testInstallWithLogger_update_auth() throws RunBuildException, IOException {
    final List<PackageSource> sources = Arrays.<PackageSource>asList(new PackageSourceImpl("foo", "use", "pwd"));
    CompositeBuildProcess bp = doBasicInstall(sources, true);

    m.checking(new Expectations(){{
      allowing(myVersion).supportAuth(); will(returnValue(true));
      allowing(myUpgradeParameters).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_SLN));

      oneOf(myActionFactory).createAuthenticateFeeds(myContext, sources, myFetchParameters);
      will(returnValue(createMockBuildProcess("auth")));

      oneOf(myActionFactory).createUpdate(with(equal(myContext)), with(equal(myUpgradeParameters)), with(any(File.class)), with(any(File.class)));
      will(returnValue(createMockBuildProcess("update")));

      oneOf(myActionFactory).createInstall(
              with(equal(myContext)),
              with(equal(myInstallParameters)),
              with(equal(false)),
              with(any(File.class)),
              with(any(File.class)));
      will(returnValue(createMockBuildProcess("install-2")));

    }});

    assertRunSuccessfully(bp, BuildFinishedStatus.FINISHED_SUCCESS);
    assertExecutedMockProcesses("version", "auth", "install", "update", "install-2", "report");

    m.assertIsSatisfied();
  }

  private CompositeBuildProcess doBasicInstall(final List<PackageSource> sources, boolean update) throws IOException, RunBuildException {
    final File slnFile = createTempFile();
    final File config = createTempFile();
    m.checking(new Expectations(){{
      allowing(myFetchParameters).getNuGetPackageSources(); will(returnValue(sources));

      oneOf(myLogger).activityStarted(with(equal("scan")), with(any(String.class)), with(any(String.class)));
      oneOf(myLogger).activityFinished(with(equal("scan")), with(any(String.class)));

      allowing(myFetchParameters).getSolutionFile(); will(returnValue(slnFile));

      oneOf(myScanner).scanResourceConfig(
              with(equal(myLogger)),
              with(equal(slnFile)),
              with(any(File.class)));
      will(returnValue(Arrays.asList(config)));

      oneOf(myActionFactory).createUsageReport(
              with(equal(myContext)),
              with(equal(config)),
              with(any(File.class)));
      will(returnValue(createMockBuildProcess("report")));

      oneOf(myActionFactory).createInstall(
              with(equal(myContext)),
              with(equal(myInstallParameters)),
              with(equal(false)),
              with(equal(config)),
              with(any(File.class)));
      will(returnValue(createMockBuildProcess("install")));

      allowing(myVersion).supportInstallNoCache(); will(returnValue(false));
    }});

    CompositeBuildProcess bp = new CompositeBuildProcessImpl();
    InstallStages is = new InstallStagesImpl(bp);
    myBuilder.buildStages(is, myContext, myFetchParameters, myInstallParameters, update ? myUpgradeParameters : null);

    return bp;
  }


}
