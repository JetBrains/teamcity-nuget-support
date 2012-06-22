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
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
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
 * Date: 08.07.11 1:36
 */
public class NuGetInstallPackageActionFactoryTest extends NuGetActionFactoryTestCase {
  protected PackagesInstallParameters ps;
  protected File myTarget;
  protected File myConfig;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    ps = m.mock(PackagesInstallParameters.class);
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

      allowing(ps).getExcludeVersion(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              myNuGetPath.getPath(),
              Arrays.asList("install", myConfig.getPath(), "-OutputDirectory", myTarget.getPath()),
              myConfig.getParentFile(),
              Collections.singletonMap("EnableNuGetPackageRestore", "True")
      );
    }});

    i.createInstall(ctx, ps, false, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_no_sources_excludeVersion() throws RunBuildException, IOException {
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Collections.<PackageSource>emptyList()));
      allowing(ps).getExcludeVersion(); will(returnValue(true));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              myNuGetPath.getPath(),
              Arrays.asList("install", myConfig.getPath(), "-ExcludeVersion", "-OutputDirectory", myTarget.getPath()),
              myConfig.getParentFile(),
              Collections.singletonMap("EnableNuGetPackageRestore", "True")
      );
    }});

    i.createInstall(ctx, ps, false, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_sources() throws RunBuildException, IOException {
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(PackageSourceImpl.convert("aaa", "bbb")));
      allowing(ps).getExcludeVersion(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              myNuGetPath.getPath(),
              Arrays.asList("install", myConfig.getPath(), "-OutputDirectory", myTarget.getPath(), "-Source", "aaa", "-Source", "bbb"),
              myConfig.getParentFile(),
              Collections.singletonMap("EnableNuGetPackageRestore", "True")
      );
    }});

    i.createInstall(ctx, ps, false, myConfig, myTarget);
    m.assertIsSatisfied();
  }
}
