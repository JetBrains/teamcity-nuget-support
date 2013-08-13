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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.runner.install.InstallStages;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesPostUpgradeInstallBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesUpdateBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 20:01
 */
public class PackageInstallerBuilderUpdateTest extends PackageInstallerBuilderTestBase {
  @NotNull
  @Override
  protected Collection<PackagesInstallerAdapter> createBuilder(@NotNull InstallStages stages) {
    return Arrays.asList(
            new PackagesUpdateBuilder(
                    myActionFactory,
                    stages.getUpdateStage(),
                    myContext,
                    myUpdate),
            new PackagesPostUpgradeInstallBuilder(
                    myActionFactory,
                    stages.getPostUpdateStart(),
                    myContext,
                    myInstall));
  }

  @Test
  public void test_install_update_per_sln() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myUpdate).getUpdateMode();
      will(returnValue(PackagesUpdateMode.FOR_SLN));

      oneOf(myActionFactory).createUpdate(myContext, myUpdate, mySln, myTaget);
      will(returnValue(createMockBuildProcess("u1")));

      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("i1")));
    }});

    doTest(t(myConfig), t("u1", "i1"));
  }

  @Test
  public void test_restore_update_per_sln() throws RunBuildException {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;

    m.checking(new Expectations(){{
      allowing(myUpdate).getUpdateMode();
      will(returnValue(PackagesUpdateMode.FOR_SLN));

      oneOf(myActionFactory).createUpdate(myContext, myUpdate, mySln, myTaget);
      will(returnValue(createMockBuildProcess("u1")));

      oneOf(myActionFactory).createRestore(myContext, myInstall, mySln, myTaget);
      will(returnValue(createMockBuildProcess("i1")));
    }});

    doTest(t(myConfig), t("u1", "i1"));
  }


  @Test
  public void test_install_update_per_config() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myUpdate).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG));

      oneOf(myActionFactory).createUpdate(myContext, myUpdate, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("update")));

      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("install")));
    }});

    doTest(t(myConfig), t("update", "install"));
  }

  @Test
  public void test_install_update_per_config_many() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myUpdate).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG));

      oneOf(myActionFactory).createUpdate(myContext, myUpdate, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("update1")));
      oneOf(myActionFactory).createUpdate(myContext, myUpdate, myConfig2, myTaget);
      will(returnValue(createMockBuildProcess("update2")));

      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("install1")));
      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig2, myTaget);
      will(returnValue(createMockBuildProcess("install2")));
    }});

    doTest(t(myConfig, myConfig2), t("update1", "update2", "install1", "install2"));
  }

  @Test
  public void test_install_update_per_config_many_removed() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myUpdate).getUpdateMode(); will(returnValue(PackagesUpdateMode.FOR_EACH_PACKAGES_CONFIG));

      oneOf(myActionFactory).createUpdate(myContext, myUpdate, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("update1", REMOVE_CONFIG_ACTION)));
      oneOf(myActionFactory).createUpdate(myContext, myUpdate, myConfig2, myTaget);
      will(returnValue(createMockBuildProcess("update2")));

      never(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("install1")));

      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig2, myTaget);
      will(returnValue(createMockBuildProcess("install2")));

      allowing(myLogger).warning(with(any(String.class)));
    }});

    doTest(t(myConfig, myConfig2), t("update1", "update2", "install2"));
  }

  @Test
  public void test_install_update_per_sln_many() throws RunBuildException {
    m.checking(new Expectations() {{
      allowing(myUpdate).getUpdateMode();
      will(returnValue(PackagesUpdateMode.FOR_SLN));

      oneOf(myActionFactory).createUpdate(myContext, myUpdate, mySln, myTaget);
      will(returnValue(createMockBuildProcess("update")));

      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("install1")));
      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig2, myTaget);
      will(returnValue(createMockBuildProcess("install2")));
    }});

    doTest(t(myConfig, myConfig2), t("update", "install1", "install2"));
  }

  @Test
  public void test_install_update_per_sln_many_project_removed() throws RunBuildException {
    m.checking(new Expectations() {{
      allowing(myUpdate).getUpdateMode();
      will(returnValue(PackagesUpdateMode.FOR_SLN));

      oneOf(myActionFactory).createUpdate(myContext, myUpdate, mySln, myTaget);
      will(returnValue(createMockBuildProcess("update", REMOVE_CONFIG_ACTION)));

      never(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("install1")));

      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig2, myTaget);
      will(returnValue(createMockBuildProcess("install2")));

      allowing(myLogger).warning(with(any(String.class)));
    }});

    doTest(t(myConfig, myConfig2), t("update", "install2"));
  }


  private final Runnable REMOVE_CONFIG_ACTION = new Runnable() {
    public void run() {
      FileUtil.delete(myConfig);
    }
  };

}
