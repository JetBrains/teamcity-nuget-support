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

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutor;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutorImpl;
import jetbrains.buildServer.nuget.server.exec.NuGetOutputProcessor;
import jetbrains.buildServer.nuget.server.exec.NuGetTeamCityProvider;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:40
 */
public class NuGetExecutorTest extends BaseTestCase {
  private Mockery m;
  private NuGetTeamCityProvider info;
  private NuGetExecutor exec;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    info = m.mock(NuGetTeamCityProvider.class);
    exec = new NuGetExecutorImpl(info);

    m.checking(new Expectations(){{
      allowing(info).getNuGetRunnerPath(); will(returnValue(Paths.getNuGetRunnerPath()));
    }});
  }

  @Test
  public void test_ping_1_4() {
    doPingTest(NuGet.NuGet_1_4);
  }

  @Test
  public void test_ping_1_5() {
    doPingTest(NuGet.NuGet_1_5);
  }

  private void doPingTest(NuGet nuget) {

    int code = exec.executeNuGet(
            nuget.getPath(),
            Arrays.asList("TeamCity.Ping"),
            new NuGetOutputProcessor<Integer>() {
              private int myExitCode;
      public void onStdOutput(String text) {
        System.out.println(text);
      }

      public void onStdError(String text) {
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
