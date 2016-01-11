/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration;


import jetbrains.buildServer.TempFolderProvider;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutor;
import jetbrains.buildServer.nuget.server.exec.NuGetOutputProcessor;
import jetbrains.buildServer.nuget.server.exec.impl.NuGetExecutorImpl;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:40
 */
public class NuGetExecutorTest extends IntegrationTestBase {
  private Mockery m;
  private NuGetTeamCityProvider info;
  private NuGetExecutor exec;
  private SystemInfo mySystemInfo;
  private TempFolderProvider myTempDir;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    info = m.mock(NuGetTeamCityProvider.class);
    mySystemInfo = m.mock(SystemInfo.class);
    myTempDir = m.mock(TempFolderProvider.class);
    exec = new NuGetExecutorImpl(info, mySystemInfo, myTempDir);

    m.checking(new Expectations(){{
      allowing(info).getNuGetRunnerPath(); will(returnValue(Paths.getNuGetRunnerPath()));
      allowing(info).getCredentialProviderHomeDirectory(); will(returnValue(Paths.getCredentialProviderHomeDirectory()));
      allowing(myTempDir).getTempDirectory(); will(returnValue(Paths.getTestDataPath()));
    }});
  }

  private void setIsWindows(final boolean isWindows) {
    m.checking(new Expectations(){{
      allowing(mySystemInfo).canStartNuGetProcesses(); will(returnValue(isWindows));
    }});
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_ping_windows(@NotNull final NuGet nuget) throws NuGetExecutionException {
    setIsWindows(true);
    doPingTest(nuget);
  }

  @Test
  public void test_does_not_run_on_linux() throws NuGetExecutionException {
    setIsWindows(false);
    try {
      doPingTest(NuGet.NuGet_1_8);
    } catch (NuGetExecutionException e) {
      return;
    }
    Assert.fail("Exception expected");
  }

  private void doPingTest(NuGet nuget) throws NuGetExecutionException {
    int code = exec.executeNuGet(
            nuget.getPath(),
            Collections.singletonList("TeamCity.Ping"), Collections.<PackageSource>emptyList(),
            new NuGetOutputProcessor<Integer>() {
              private int myExitCode;
      public void onStdOutput(@NotNull String text) {
        System.out.println(text);
      }

      public void onStdError(@NotNull String text) {
        System.out.println(text);
      }

      public void onFinished(int exitCode) {
        System.out.println("Exit Code: " + exitCode);
        myExitCode = exitCode;
      }

      @NotNull
      public Integer getResult() {
        return myExitCode;
      }
    });

    Assert.assertEquals(code, 0);
  }
}
