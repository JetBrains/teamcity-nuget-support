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

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.runner.pack.PackRunner;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import jetbrains.buildServer.util.FileUtil;
import junit.framework.Assert;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 24.08.11 12:31
 */
public class PackRunnerTest extends BuildProcessTestCase {
  private Mockery m;
  private NuGetActionFactory myActionFactory;
  private PackagesParametersFactory myParametersFactory;
  private SmartDirectoryCleaner myCleaner;
  private AgentRunningBuild myBuild;
  private BuildRunnerContext myContext;
  private NuGetPackParameters myPackParameters;
  private BuildProcess myProc;
  private BuildProgressLogger myLogger;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myActionFactory = m.mock(NuGetActionFactory.class);
    myParametersFactory = m.mock(PackagesParametersFactory.class);
    myCleaner = m.mock(SmartDirectoryCleaner.class);
    myBuild = m.mock(AgentRunningBuild.class);
    myContext = m.mock(BuildRunnerContext.class);
    myPackParameters = m.mock(NuGetPackParameters.class);
    myProc = m.mock(BuildProcess.class);
    myLogger = m.mock(BuildProgressLogger.class);

    m.checking(new Expectations(){{
      allowing(myParametersFactory).loadPackParameters(myContext); will(returnValue(myPackParameters));
      allowing(myActionFactory).createPack(myContext, myPackParameters); will(returnValue(myProc));

      allowing(myBuild).getBuildLogger(); will(returnValue(myLogger));
      allowing(myContext).getBuild(); will(returnValue(myBuild));

      allowing(myLogger).message(with(any(String.class)));
      allowing(myLogger).activityStarted(with(any(String.class)), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityStarted(with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(any(String.class)), with(any(String.class)));


      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));
    }});
  }

  @Test
  public void test_packRunner_outputDirectory_notCleaned() throws RunBuildException, IOException {
    m.checking(new Expectations(){{
      allowing(myPackParameters).cleanOutputDirectory(); will(returnValue(false));
      allowing(myPackParameters).getOutputDirectory(); will(returnValue(createTempDir()));
    }});

    final PackRunner runner = new PackRunner(myActionFactory, myParametersFactory, myCleaner);
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);
  }

  @Test
  public void test_packRunner_outputDirectoryCleaned() throws RunBuildException, IOException {
    final File temp = createTempDir();
    m.checking(new Expectations(){{
      allowing(myPackParameters).cleanOutputDirectory(); will(returnValue(true));
      allowing(myPackParameters).getOutputDirectory();will(returnValue(temp));

      oneOf(myCleaner).cleanFolder(with(equal(temp)), with(any(SmartDirectoryCleanerCallback.class)));
    }});

    FileUtil.delete(temp);
    final PackRunner runner = new PackRunner(myActionFactory, myParametersFactory, myCleaner);
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    Assert.assertTrue(temp.isDirectory());
  }

  @Test
  public void test_packRunner_outputDirectoryCleaned_error() throws RunBuildException, IOException {
    final File temp = createTempDir();
    m.checking(new Expectations(){{
      allowing(myPackParameters).cleanOutputDirectory(); will(returnValue(true));
      allowing(myPackParameters).getOutputDirectory();will(returnValue(temp));

      allowing(myLogger).error(with(any(String.class)));

      oneOf(myCleaner).cleanFolder(with(equal(temp)), with(new BaseMatcher<SmartDirectoryCleanerCallback>() {
        public boolean matches(Object o) {
          SmartDirectoryCleanerCallback cb = (SmartDirectoryCleanerCallback) o;
          cb.logFailedToCleanFile(temp);

          return true;
        }

        public void describeTo(Description description) {
          description.appendText("Callback with failure");
        }
      }));
    }});

    final PackRunner runner = new PackRunner(myActionFactory, myParametersFactory, myCleaner);
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_FAILED);
  }

}
