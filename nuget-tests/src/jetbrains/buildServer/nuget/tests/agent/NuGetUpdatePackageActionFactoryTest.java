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
import jetbrains.buildServer.nuget.agent.install.PackagesUpdateParameters;
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
 * Date: 10.07.11 14:29
 */
public class NuGetUpdatePackageActionFactoryTest extends BaseTestCase {
  private Mockery m;
  private CommandlineBuildProcessFactory myProcessFactory;
  private NuGetActionFactoryImpl i;
  private BuildRunnerContext ctx;
  private PackagesUpdateParameters ps;
  private File myTarget;
  private File myConfig;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myProcessFactory = m.mock(CommandlineBuildProcessFactory.class);
    i = new NuGetActionFactoryImpl(myProcessFactory);
    ctx = m.mock(BuildRunnerContext.class);
    ps = m.mock(PackagesUpdateParameters.class);

    myTarget = createTempDir();
    myConfig = createTempFile();
  }

  @Test
  public void test_no_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(ps).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(ps).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getUseSafeUpdate(); will(returnValue(false));
      allowing(ps).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget,
              Arrays.asList("update", myConfig.getPath(), "-RepositoryPath", myTarget.getPath()),
              myConfig.getParentFile()
      );
    }});

    i.createUpdate(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_packageIds() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(ps).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(ps).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getUseSafeUpdate(); will(returnValue(false));
      allowing(ps).getPackagesToUpdate(); will(returnValue(Arrays.asList("aaa", "bbb")));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget,
              Arrays.asList("update", myConfig.getPath(), "-RepositoryPath", myTarget.getPath(), "-Id", "aaa", "-Id", "bbb"),
              myConfig.getParentFile()
      );
    }});

    i.createUpdate(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_safe() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(ps).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(ps).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getUseSafeUpdate(); will(returnValue(true));
      allowing(ps).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget,
              Arrays.asList("update", myConfig.getPath(), "-Safe", "-RepositoryPath", myTarget.getPath()),
              myConfig.getParentFile()
      );
    }});

    i.createUpdate(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(ps).getNuGetPackageSources(); will(returnValue(Arrays.asList("aaa", "bbb")));
      allowing(ps).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getUseSafeUpdate(); will(returnValue(false));
      allowing(ps).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget,
              Arrays.asList("update", myConfig.getPath(), "-RepositoryPath", myTarget.getPath(), "-Source", "aaa", "-Source", "bbb"),
              myConfig.getParentFile()
      );
    }});

    i.createUpdate(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }


}
