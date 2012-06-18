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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesInstallerBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesReportBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesUpdateBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerCallback;
import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 20:26
 */
public class PackagesInstallerBuilderTest extends BaseTestCase {
  private Mockery m;
  private PackagesInstallerCallback builderUpdate;
  private PackagesInstallerCallback builderInstall;
  private PackagesInstallerCallback builderReport;
  private NuGetActionFactory factory;
  private BuildProcessContinuation install;
  private BuildProcessContinuation update;
  private BuildProcessContinuation postUpdate;
  private BuildProcessContinuation report;
  private BuildRunnerContext context;
  private NuGetFetchParameters nugetSettings;
  private PackagesInstallParameters installParameters;
  private PackagesUpdateParameters updateParameters;
  private File myTaget;
  private File mySln;
  private File myConfig;
  private File myConfig2;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    final File root = createTempDir();
    myTaget = new File(root, "packages"){{mkdirs();}};
    mySln = new File(root, "project.sln") {{createNewFile();}};
    myConfig = new File(new File(root, "project"){{mkdirs();}}, "packages.config"){{createNewFile();}};
    myConfig2 = new File(new File(root, "project2"){{mkdirs();}}, "packages.config"){{createNewFile();}};

    m = new Mockery();
    factory = m.mock(NuGetActionFactory.class);
    install = m.mock(BuildProcessContinuation.class, "install");
    update = m.mock(BuildProcessContinuation.class, "update");
    postUpdate = m.mock(BuildProcessContinuation.class, "post-install");
    report = m.mock(BuildProcessContinuation.class, "report");
    context = m.mock(BuildRunnerContext.class);
    installParameters = m.mock(PackagesInstallParameters.class);
    updateParameters = m.mock(PackagesUpdateParameters.class);
    nugetSettings = m.mock(NuGetFetchParameters.class);

    builderInstall = new PackagesInstallerBuilder(
            factory,
            install,
            context,
            installParameters);

    builderUpdate = new PackagesUpdateBuilder(
            factory,
            update,
            postUpdate,
            context,
            installParameters,
            updateParameters
    );

    builderReport = new PackagesReportBuilder(
            factory,
            report,
            context);

    m.checking(new Expectations(){{
      allowing(installParameters).getNuGetParameters(); will(returnValue(nugetSettings));
      allowing(updateParameters).getNuGetParameters(); will(returnValue(nugetSettings));
    }});
  }

  @Test
  public void test_install_no_update() throws RunBuildException {
    final BuildProcess bp = m.mock(BuildProcess.class, "bp");
    m.checking(new Expectations(){{
      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bp));

      oneOf(install).pushBuildProcess(bp);
    }});

    builderInstall.onSolutionFileFound(mySln, myTaget);
    builderInstall.onPackagesConfigFound(myConfig, myTaget);

    m.assertIsSatisfied();
  }

  @Test
  public void test_report() throws RunBuildException {
    final BuildProcess bp = m.mock(BuildProcess.class, "bp");
    m.checking(new Expectations(){{
      oneOf(factory).createUsageReport(context, myConfig, myTaget); will(returnValue(bp));
      oneOf(report).pushBuildProcess(bp);
    }});

    builderReport.onSolutionFileFound(mySln, myTaget);
    builderReport.onPackagesConfigFound(myConfig, myTaget);

    m.assertIsSatisfied();
  }

  @Test
  public void test_install_no_update_may() throws RunBuildException {
    final BuildProcess bp1 = m.mock(BuildProcess.class, "bp-install-1");
    final BuildProcess bp2 = m.mock(BuildProcess.class, "bp-install-2");
    m.checking(new Expectations(){{
      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bp1));
      oneOf(factory).createInstall(context, installParameters, myConfig2, myTaget);
      will(returnValue(bp2));

      oneOf(install).pushBuildProcess(bp1);
      oneOf(install).pushBuildProcess(bp2);
    }});

    builderInstall.onSolutionFileFound(mySln, myTaget);
    builderInstall.onPackagesConfigFound(myConfig, myTaget);
    builderInstall.onPackagesConfigFound(myConfig2, myTaget);

    m.assertIsSatisfied();
  }

  @Test
  public void test_install_update_per_sln() throws RunBuildException {
    final BuildProcess bpInstall = m.mock(BuildProcess.class, "bp-install");
    final BuildProcess bpUpdate = m.mock(BuildProcess.class, "bp-update");
    final BuildProcess bpPostInstall = m.mock(BuildProcess.class, "bp-post-install");

    m.checking(new Expectations(){{
      allowing(updateParameters).getUpdateMode();
      will(returnValue(PackagesUpdateMode.FOR_SLN));

      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bpInstall));

      oneOf(factory).createUpdate(context, updateParameters, mySln, myTaget);
      will(returnValue(bpUpdate));

      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bpPostInstall));

      oneOf(update).pushBuildProcess(bpUpdate);
      oneOf(install).pushBuildProcess(bpInstall);
      oneOf(postUpdate).pushBuildProcess(bpPostInstall);

    }});

    builderInstall.onSolutionFileFound(mySln, myTaget);
    builderUpdate.onSolutionFileFound(mySln, myTaget);
    builderInstall.onPackagesConfigFound(myConfig, myTaget);
    builderUpdate.onPackagesConfigFound(myConfig, myTaget);

    m.assertIsSatisfied();
  }


  @Test
  public void test_install_update_per_sln_many() throws RunBuildException {
    final BuildProcess bpInstall1 = m.mock(BuildProcess.class, "bp-install-1");
    final BuildProcess bpInstall2 = m.mock(BuildProcess.class, "bp-install-2");
    final BuildProcess bpUpdate = m.mock(BuildProcess.class, "bp-update");
    final BuildProcess bpPostInstall1 = m.mock(BuildProcess.class, "bp-post-install-1");
    final BuildProcess bpPostInstall2 = m.mock(BuildProcess.class, "bp-post-install-2");

    m.checking(new Expectations(){{
      allowing(updateParameters).getUpdateMode();
      will(returnValue(PackagesUpdateMode.FOR_SLN));

      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bpInstall1));

      oneOf(factory).createInstall(context, installParameters, myConfig2, myTaget);
      will(returnValue(bpInstall2));

      oneOf(factory).createUpdate(context, updateParameters, mySln, myTaget);
      will(returnValue(bpUpdate));

      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bpPostInstall1));
      oneOf(factory).createInstall(context, installParameters, myConfig2, myTaget);
      will(returnValue(bpPostInstall2));

      oneOf(update).pushBuildProcess(bpUpdate);
      oneOf(install).pushBuildProcess(bpInstall1);
      oneOf(install).pushBuildProcess(bpInstall2);
      oneOf(postUpdate).pushBuildProcess(bpPostInstall1);
      oneOf(postUpdate).pushBuildProcess(bpPostInstall2);

    }});

    builderInstall.onSolutionFileFound(mySln, myTaget);
    builderUpdate.onSolutionFileFound(mySln, myTaget);

    builderInstall.onPackagesConfigFound(myConfig, myTaget);
    builderUpdate.onPackagesConfigFound(myConfig, myTaget);

    builderInstall.onPackagesConfigFound(myConfig2, myTaget);
    builderUpdate.onPackagesConfigFound(myConfig2, myTaget);

    m.assertIsSatisfied();
  }

  @Test
  public void test_install_update_per_config() throws RunBuildException {
    final BuildProcess bpInstall = m.mock(BuildProcess.class, "bp-install");
    final BuildProcess bpUpdate = m.mock(BuildProcess.class, "bp-update");
    final BuildProcess bpPostInstall = m.mock(BuildProcess.class, "bp-post-install");

    m.checking(new Expectations(){{
      allowing(updateParameters).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG));

      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bpInstall));

      oneOf(factory).createUpdate(context, updateParameters, myConfig, myTaget);
      will(returnValue(bpUpdate));

      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bpPostInstall));

      oneOf(update).pushBuildProcess(bpUpdate);
      oneOf(install).pushBuildProcess(bpInstall);
      oneOf(postUpdate).pushBuildProcess(bpPostInstall);

    }});

    builderInstall.onSolutionFileFound(mySln, myTaget);
    builderUpdate.onSolutionFileFound(mySln, myTaget);
    builderInstall.onPackagesConfigFound(myConfig, myTaget);
    builderUpdate.onPackagesConfigFound(myConfig, myTaget);

    m.assertIsSatisfied();
  }

  @Test
  public void test_install_update_per_config_many() throws RunBuildException {
    final BuildProcess bpInstall1 = m.mock(BuildProcess.class, "bp-install-1");
    final BuildProcess bpInstall2 = m.mock(BuildProcess.class, "bp-install-2");
    final BuildProcess bpUpdate1 = m.mock(BuildProcess.class, "bp-update-1");
    final BuildProcess bpUpdate2 = m.mock(BuildProcess.class, "bp-update-2");
    final BuildProcess bpPostInstall1 = m.mock(BuildProcess.class, "bp-post-install-1");
    final BuildProcess bpPostInstall2 = m.mock(BuildProcess.class, "bp-post-install-2");

    m.checking(new Expectations(){{
      allowing(updateParameters).getUpdateMode();
      will(returnValue(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG));

      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bpInstall1));
      oneOf(factory).createInstall(context, installParameters, myConfig2, myTaget);
      will(returnValue(bpInstall2));

      oneOf(factory).createUpdate(context, updateParameters, myConfig, myTaget);
      will(returnValue(bpUpdate1));
      oneOf(factory).createUpdate(context, updateParameters, myConfig2, myTaget);
      will(returnValue(bpUpdate2));

      oneOf(factory).createInstall(context, installParameters, myConfig, myTaget);
      will(returnValue(bpPostInstall1));
      oneOf(factory).createInstall(context, installParameters, myConfig2, myTaget);
      will(returnValue(bpPostInstall2));

      oneOf(update).pushBuildProcess(bpUpdate1);
      oneOf(update).pushBuildProcess(bpUpdate2);
      oneOf(install).pushBuildProcess(bpInstall1);
      oneOf(install).pushBuildProcess(bpInstall2);
      oneOf(postUpdate).pushBuildProcess(bpPostInstall1);
      oneOf(postUpdate).pushBuildProcess(bpPostInstall2);
    }});

    builderInstall.onSolutionFileFound(mySln, myTaget);
    builderUpdate.onSolutionFileFound(mySln, myTaget);

    builderInstall.onPackagesConfigFound(myConfig, myTaget);
    builderUpdate.onPackagesConfigFound(myConfig, myTaget);

    builderInstall.onPackagesConfigFound(myConfig2, myTaget);
    builderUpdate.onPackagesConfigFound(myConfig2, myTaget);

    m.assertIsSatisfied();
  }
}
