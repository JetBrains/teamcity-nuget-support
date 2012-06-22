/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetVersionHolderImpl;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 20:08
 */
public class VersionCommandIntegrationTest extends IntegrationTestBase {

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m.checking(new Expectations(){{
      allowing(myLogger).activityStarted(with(equal("version")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("version")), with(any(String.class)));
    }});
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_version(@NotNull final NuGet nuget) throws IOException, RunBuildException {

    m.checking(new Expectations(){{
      allowing(myNuGet).getNuGetExeFile(); will(returnValue(nuget.getPath()));
    }});

    NuGetVersionHolderImpl version = new NuGetVersionHolderImpl();
    BuildProcess cmd = myActionFactory.createVersionCheckCommand(myContext, version, myNuGet);
    assertRunSuccessfully(cmd, BuildFinishedStatus.FINISHED_SUCCESS);


    Assert.assertEquals(version.getNuGetVerion().supportInstallNoCache(), noCache(nuget));
    Assert.assertEquals(version.getNuGetVerion().supportAuth(), noAuth(nuget));
    m.assertIsSatisfied();
  }

  private boolean noAuth(NuGet nuget) {
    switch (nuget) {
      case NuGet_1_6:
      case NuGet_1_7:
      case NuGet_1_8:
        return false;
      default:
        return true;
    }
  }

  private boolean noCache(@NotNull NuGet nuget) {
    switch (nuget) {
      case NuGet_1_6:
        return false;
      default:
        return true;
    }
  }
}
