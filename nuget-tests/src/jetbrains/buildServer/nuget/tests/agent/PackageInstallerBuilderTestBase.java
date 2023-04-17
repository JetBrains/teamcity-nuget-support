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

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.install.InstallStages;
import jetbrains.buildServer.nuget.agent.runner.install.impl.InstallStagesImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.BuildProcessContinuation;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static jetbrains.buildServer.nuget.common.PackagesInstallMode.VIA_INSTALL;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 20:48
 */
public abstract class PackageInstallerBuilderTestBase extends IntegrationTestBase {
  private List<String> myExecutedProcesses;
  private List<BuildProcess> myProcessesList;

  protected Mockery m;
  protected NuGetActionFactory myActionFactory;
  protected Collection<PackagesInstallerAdapter> myBuilders;
  protected BuildRunnerContext myContext;
  protected AgentRunningBuild myBuild;
  protected BuildProgressLogger myLogger;
  protected PackagesInstallParameters myInstall;
  protected PackagesUpdateParameters myUpdate;
  protected File myTaget;
  protected File mySln;
  protected File myConfig;
  protected File myConfig2;

  protected PackagesInstallMode myInstallMode;

  @Override
  @BeforeMethod
  @SuppressWarnings("ResultOfMethodCallIgnored")
  protected void setUp() throws Exception {
    super.setUp();

    final File root = createTempDir();
    myTaget = new File(root, "packages"){{mkdirs();}};
    mySln = new File(root, "project.sln") {{createNewFile();}};
    myConfig = new File(new File(root, "project"){{mkdirs();}}, "packages.config"){{createNewFile();}};
    myConfig2 = new File(new File(root, "project2"){{mkdirs();}}, "packages.config"){{createNewFile();}};
    myProcessesList = new ArrayList<BuildProcess>();
    final InstallStages stages = new InstallStagesImpl(cont("list", myProcessesList));

    m = TCJMockUtils.createInstance();
    myInstallMode = VIA_INSTALL;
    myExecutedProcesses = new ArrayList<String>();
    myActionFactory = m.mock(NuGetActionFactory.class);
    myContext = m.mock(BuildRunnerContext.class);
    myBuild = m.mock(AgentRunningBuild.class);
    myLogger = m.mock(BuildProgressLogger.class);

    myInstall = m.mock(PackagesInstallParameters.class);
    myUpdate = m.mock(PackagesUpdateParameters.class);

    myBuilders = new ArrayList<PackagesInstallerAdapter>(createBuilder(stages));

    m.checking(new Expectations(){{
      allowing(myContext).getBuild(); will(returnValue(myBuild));
      allowing(myBuild).getBuildLogger(); will(returnValue(myLogger));

      allowing(myInstall).getInstallMode(); will(new CustomAction("return myInstallMode") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myInstallMode;
        }
      });
    }});
  }

  @NotNull
  protected abstract Collection<PackagesInstallerAdapter> createBuilder(@NotNull InstallStages stages);

  @NotNull
  protected BuildProcess createMockBuildProcess(@NotNull final String name) {
    return createMockBuildProcess(name, null);
  }

  @NotNull
  protected BuildProcess createMockBuildProcess(@NotNull final String name, @Nullable final Runnable action) {
    return new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        myExecutedProcesses.add(name);
        if (action != null) action.run();
        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    };
  }

  @NotNull
  private BuildProcessContinuation cont(@NotNull final String name, @NotNull final List<BuildProcess> proc) {
    return new BuildProcessContinuation() {
      public void pushBuildProcess(@NotNull BuildProcess process) {
        proc.add(process);
      }

      @Override
      public String toString() {
        return "Cont@" + name;
      }
    };
  }


  protected void doTest(File[] configs, String[] procs) throws RunBuildException {
    for (PackagesInstallerAdapter builder : myBuilders) {
      builder.onSolutionFileFound(mySln, myTaget);
      for (File config : configs) {
        builder.onPackagesConfigFound(config, myTaget);
      }
    }

    for (BuildProcess update : myProcessesList) {
      assertRunSuccessfully(update, BuildFinishedStatus.FINISHED_SUCCESS);
    }

    Assert.assertEquals(Arrays.asList(procs), myExecutedProcesses);

    m.assertIsSatisfied();
  }

  protected static <T> T[] t(T...t) {
    return t;
  }

}
