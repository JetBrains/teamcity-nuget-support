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

package jetbrains.buildServer.nuget.tests.agent.factory;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.tests.agent.PackageSourceImpl;
import org.jmock.Expectations;
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
public class NuGetUpdatePackageActionFactoryTest extends NuGetActionFactoryTestCase {
  private PackagesUpdateParameters ps;
  private File myTarget;
  private File myConfig;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ps = m.mock(PackagesUpdateParameters.class);

    myTarget = createTempDir();
    myConfig = createTempFile();

    m.checking(new Expectations(){{
      allowing(ps).getNuGetParameters(); will(returnValue(nugetParams));
    }});
  }

  @Test
  public void test_no_sources() throws RunBuildException, IOException {
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Collections.<PackageSource>emptyList()));
      allowing(ps).getUseSafeUpdate(); will(returnValue(false));
      allowing(ps).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(ps).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              myNuGetPath.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-Verbose", "-RepositoryPath", myTarget.getPath()),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_packageIds() throws RunBuildException, IOException {
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Collections.<PackageSource>emptyList()));
      allowing(ps).getUseSafeUpdate(); will(returnValue(false));
      allowing(ps).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(ps).getPackagesToUpdate(); will(returnValue(Arrays.asList("aaa", "bbb")));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              myNuGetPath.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-Verbose", "-RepositoryPath", myTarget.getPath(), "-Id", "aaa", "-Id", "bbb"),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_safe() throws RunBuildException, IOException {
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Collections.<PackageSource>emptyList()));
      allowing(ps).getUseSafeUpdate(); will(returnValue(true));
      allowing(ps).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(ps).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              myNuGetPath.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-Safe", "-Verbose", "-RepositoryPath", myTarget.getPath()),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_sources() throws RunBuildException, IOException {
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(PackageSourceImpl.convert("aaa", "bbb")));
      allowing(ps).getUseSafeUpdate(); will(returnValue(false));
      allowing(ps).getIncludePrereleasePackages(); will(returnValue(false));
      allowing(ps).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              myNuGetPath.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-Verbose", "-RepositoryPath", myTarget.getPath(), "-Source", "aaa", "-Source", "bbb"),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_sources_prerelease() throws RunBuildException, IOException {
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(PackageSourceImpl.convert("aaa", "bbb")));
      allowing(ps).getUseSafeUpdate(); will(returnValue(false));
      allowing(ps).getIncludePrereleasePackages(); will(returnValue(true));
      allowing(ps).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              myNuGetPath.getPath(),
              Arrays.asList("update", myConfig.getPath(), "-Prerelease", "-Verbose", "-RepositoryPath", myTarget.getPath(), "-Source", "aaa", "-Source", "bbb"),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createUpdate(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

}
