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

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.impl.CommandFactoryImpl;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetActionFactoryImpl;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created 13.08.13 12:05
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetRestorePackageActionFactoryTest extends BaseTestCase {
  private Mockery m;
  private CommandlineBuildProcessFactory myProcessFactory;
  private NuGetActionFactoryImpl i;
  private BuildRunnerContext ctx;
  private PackagesInstallParameters myInstallParameters;
  private NuGetFetchParameters myFetchParams;
  private File mySolution;
  private BuildParametersMap myBuildParametersMap;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myProcessFactory = m.mock(CommandlineBuildProcessFactory.class);
    PackageUsages pu = m.mock(PackageUsages.class);
    i = new NuGetActionFactoryImpl(myProcessFactory, pu, new CommandFactoryImpl());
    ctx = m.mock(BuildRunnerContext.class);
    myInstallParameters = m.mock(PackagesInstallParameters.class);
    myFetchParams = m.mock(NuGetFetchParameters.class);

    mySolution = createTempFile();

    myBuildParametersMap = m.mock(BuildParametersMap.class);

    m.checking(new Expectations(){{
      allowing(ctx).getBuildParameters(); will(returnValue(myBuildParametersMap));
      allowing(myInstallParameters).getNuGetParameters(); will(returnValue(myFetchParams));

      allowing(myFetchParams).getSolutionFile(); will(returnValue(mySolution));
    }});
  }

  @Test
  public void test_no_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(myFetchParams).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(myFetchParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(myFetchParams).getCustomCommandline();  will(returnValue(Collections.<String>emptyList()));
      allowing(myInstallParameters).getExcludeVersion(); will(returnValue(false));
      allowing(myInstallParameters).getNoCache(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("restore", mySolution.getPath()),
              mySolution.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createRestoreForSolution(ctx, myInstallParameters, mySolution);
    m.assertIsSatisfied();
  }

  @Test
  public void test_no_sources_no_cache() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(myFetchParams).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(myFetchParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(myFetchParams).getCustomCommandline();  will(returnValue(Collections.<String>emptyList()));
      allowing(myInstallParameters).getExcludeVersion(); will(returnValue(false));
      allowing(myInstallParameters).getNoCache(); will(returnValue(true));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("restore", mySolution.getPath(), "-NoCache"),
              mySolution.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createRestoreForSolution(ctx, myInstallParameters, mySolution);
    m.assertIsSatisfied();
  }

  @Test
  public void test_no_sources_excludeVersion() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(myFetchParams).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(myFetchParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(myFetchParams).getCustomCommandline();  will(returnValue(Collections.<String>emptyList()));
      allowing(myInstallParameters).getExcludeVersion(); will(returnValue(true));
      allowing(myInstallParameters).getNoCache(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("restore", mySolution.getPath(), "-ExcludeVersion"),
              mySolution.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createRestoreForSolution(ctx, myInstallParameters, mySolution);
    m.assertIsSatisfied();
  }

  @Test
  public void test_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(myFetchParams).getNuGetPackageSources(); will(returnValue(Arrays.asList("aaa", "bbb")));
      allowing(myFetchParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(myFetchParams).getCustomCommandline();  will(returnValue(Collections.<String>emptyList()));
      allowing(myInstallParameters).getExcludeVersion(); will(returnValue(false));
      allowing(myInstallParameters).getNoCache(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("restore", mySolution.getPath(), "-Source", "aaa", "-Source", "bbb"),
              mySolution.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createRestoreForSolution(ctx, myInstallParameters, mySolution);
    m.assertIsSatisfied();
  }
}

