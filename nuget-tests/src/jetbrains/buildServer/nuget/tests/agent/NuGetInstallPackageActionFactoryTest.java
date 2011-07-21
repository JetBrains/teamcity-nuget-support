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
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.install.PackageUsages;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.install.impl.NuGetActionFactoryImpl;
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
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 08.07.11 1:36
 */
public class NuGetInstallPackageActionFactoryTest extends BaseTestCase {
  private Mockery m;
  private CommandlineBuildProcessFactory myProcessFactory;
  private NuGetActionFactoryImpl i;
  private BuildRunnerContext ctx;
  private PackagesInstallParameters ps;
  private NuGetFetchParameters nugetParams;
  private File myTarget;
  private File myConfig;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myProcessFactory = m.mock(CommandlineBuildProcessFactory.class);
    PackageUsages pu = m.mock(PackageUsages.class);
    i = new NuGetActionFactoryImpl(myProcessFactory, pu);
    ctx = m.mock(BuildRunnerContext.class);
    ps = m.mock(PackagesInstallParameters.class);
    nugetParams = m.mock(NuGetFetchParameters.class);

    myTarget = createTempDir();
    myConfig = createTempFile();

    m.checking(new Expectations(){{
      allowing(ps).getNuGetParameters(); will(returnValue(nugetParams));
    }});
  }

  @Test
  public void test_no_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(nugetParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getExcludeVersion(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget,
              Arrays.asList("install", myConfig.getPath(), "-OutputDirectory", myTarget.getPath()),
              myConfig.getParentFile()
      );
    }});

    i.createInstall(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_no_sources_excludeVersion() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(nugetParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getExcludeVersion(); will(returnValue(true));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget,
              Arrays.asList("install", myConfig.getPath(), "-ExcludeVersion", "-OutputDirectory", myTarget.getPath()),
              myConfig.getParentFile()
      );
    }});

    i.createInstall(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Arrays.asList("aaa", "bbb")));
      allowing(nugetParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getExcludeVersion(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget,
              Arrays.asList("install", myConfig.getPath(), "-OutputDirectory", myTarget.getPath(), "-Source", "aaa", "-Source", "bbb"),
              myConfig.getParentFile()
      );
    }});

    i.createInstall(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }
}
