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
import jetbrains.buildServer.nuget.agent.runner.install.PackagesInstallerBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 21:02
 */
public class PackageInstallerBuilderInstallTest extends PackageInstallerBuilderTestBase {
  @NotNull
  @Override
  protected Collection<PackagesInstallerAdapter> createBuilder(@NotNull InstallStages stages) {
    return Arrays.<PackagesInstallerAdapter>asList(new PackagesInstallerBuilder(myActionFactory, stages.getInstallStage(), myContext, myInstall));
  }

  @Test
  public void test_install_no_update() throws RunBuildException {
    m.checking(new Expectations() {{
      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("b1")));
    }});

    doTest(t(myConfig), t("b1"));
  }

  @Test
  public void test_restore_no_update() throws RunBuildException {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;
    m.checking(new Expectations() {{
      oneOf(myActionFactory).createRestore(myContext, myInstall, mySln, myTaget);
      will(returnValue(createMockBuildProcess("b1")));
    }});

    doTest(t(myConfig), t("b1"));
  }

  @Test
  public void test_install_no_update_may() throws RunBuildException {
    m.checking(new Expectations(){{
      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("b1")));
      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig2, myTaget);
      will(returnValue(createMockBuildProcess("b2")));

    }});

    doTest(t(myConfig, myConfig2), t("b1", "b2"));
  }



}
