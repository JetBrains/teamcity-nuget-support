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

package jetbrains.buildServer.nuget.tests.agent.factory;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.commands.NuGetVersion;
import jetbrains.buildServer.nuget.agent.commands.NuGetVersionCallback;
import jetbrains.buildServer.nuget.tests.mocks.ListMatcher;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static jetbrains.buildServer.agent.BuildFinishedStatus.FINISHED_SUCCESS;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 13:44
 */
public class NuGetVersionActionFactoryTest extends NuGetActionFactoryTestCase {
  private NuGetVersion myVersion;
  private NuGetVersionCallback myVersionCallback;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myVersion = m.mock(NuGetVersion.class);
    myVersionCallback = m.mock(NuGetVersionCallback.class);

  }

  @Test
  public void testVersionCommand() throws IOException, RunBuildException {
    m.checking(new Expectations(){{
      oneOf(myProcessFactory).executeCommandLine(
              with(equal(ctx)),
              with(equal(myNuGetRunnerPath.getPath())),
              with((org.hamcrest.Matcher<Collection<String>>)new ListMatcher<String>(Arrays.asList(
                      equal(myNuGetPath.getPath()),
                      equal("--TeamCity.NuGetVersion"),
                      any(String.class)
              ))),
              with(equal(myWorkDir)),
              with(equal(Collections.<String, String>emptyMap()))
              );
      will(returnValue(createMockBuildProcess("run")));

      oneOf(myVersionFactory).getFromVersionFile(with(any(File.class)));
      will(returnValue(myVersion));

      oneOf(myVersionCallback).onNuGetVersionCompleted(myVersion);
    }});


    assertRunSuccessfully(i.createVersionCheckCommand(ctx, myVersionCallback, nugetParams), FINISHED_SUCCESS);
    assertExecutedMockProcesses("run");
    m.assertIsSatisfied();

  }

}
