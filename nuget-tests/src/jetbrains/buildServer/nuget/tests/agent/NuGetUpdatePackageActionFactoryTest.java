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
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.07.11 14:29
 */
public class NuGetUpdatePackageActionFactoryTest extends BaseTestCase {
  private Mockery m;
  private CommandlineBuildProcessFactory myProcessFactory;
  private NuGetActionFactoryImpl i;
  private BuildRunnerContext ctx;
  private NuGetFetchParameters myFetchParameters;
  private PackagesUpdateParameters myUpdateParameters;
  private File myTarget;
  private File myConfig;
  private BuildParametersMap myBuildParametersMap;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    myProcessFactory = m.mock(CommandlineBuildProcessFactory.class);
    PackageUsages pu = m.mock(PackageUsages.class);
    i = new NuGetActionFactoryImpl(myProcessFactory, pu, new CommandFactoryImpl());
    ctx = m.mock(BuildRunnerContext.class);
    myUpdateParameters = m.mock(PackagesUpdateParameters.class);
    myFetchParameters = m.mock(NuGetFetchParameters.class);

    myTarget = createTempDir();
    myConfig = createTempFile();

    myBuildParametersMap = m.mock(BuildParametersMap.class);

    m.checking(new Expectations(){{
      allowing(ctx).getBuildParameters(); will(returnValue(myBuildParametersMap));
      allowing(myUpdateParameters).getNuGetParameters(); will(returnValue(myFetchParameters));
    }});
  }

  @Test
  public void test_no_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(myFetchParameters).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(myFetchParameters).getNuGetExeFile();  will(returnValue(nuget));
      allowing(myUpdateParameters).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdateParameters).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(myUpdateParameters).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdateParameters).getCustomCommandline();  will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-RepositoryPath", myTarget.getPath()),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, myUpdateParameters, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_packageIds() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(myFetchParameters).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(myFetchParameters).getNuGetExeFile();  will(returnValue(nuget));
      allowing(myUpdateParameters).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdateParameters).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(myUpdateParameters).getPackagesToUpdate(); will(returnValue(Arrays.asList("aaa", "bbb")));
      allowing(myUpdateParameters).getCustomCommandline();  will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-RepositoryPath", myTarget.getPath(), "-Id", "aaa", "-Id", "bbb"),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, myUpdateParameters, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_safe() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(myFetchParameters).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(myFetchParameters).getNuGetExeFile();  will(returnValue(nuget));
      allowing(myUpdateParameters).getUseSafeUpdate(); will(returnValue(true));
      allowing(myUpdateParameters).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(myUpdateParameters).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdateParameters).getCustomCommandline();  will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-Safe", "-RepositoryPath", myTarget.getPath()),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, myUpdateParameters, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(NuGetUpdatePackageActionFactoryTest.this.myFetchParameters).getNuGetPackageSources(); will(returnValue(Arrays.asList("aaa", "bbb")));
      allowing(NuGetUpdatePackageActionFactoryTest.this.myFetchParameters).getNuGetExeFile();  will(returnValue(nuget));
      allowing(myUpdateParameters).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdateParameters).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(myUpdateParameters).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdateParameters).getCustomCommandline();  will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-RepositoryPath", myTarget.getPath(), "-Source", "aaa", "-Source", "bbb"),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, myUpdateParameters, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_sources_prerelease() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(NuGetUpdatePackageActionFactoryTest.this.myFetchParameters).getNuGetPackageSources(); will(returnValue(Arrays.asList("aaa", "bbb")));
      allowing(NuGetUpdatePackageActionFactoryTest.this.myFetchParameters).getNuGetExeFile();  will(returnValue(nuget));
      allowing(myUpdateParameters).getUseSafeUpdate(); will(returnValue(false));
      allowing(myUpdateParameters).getIncludePrereleasePackages(); will(returnValue(true));
      allowing(myUpdateParameters).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdateParameters).getCustomCommandline();  will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-Prerelease", "-RepositoryPath", myTarget.getPath(), "-Source", "aaa", "-Source", "bbb"),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, myUpdateParameters, myConfig, myTarget);
    m.assertIsSatisfied();
  }

}
