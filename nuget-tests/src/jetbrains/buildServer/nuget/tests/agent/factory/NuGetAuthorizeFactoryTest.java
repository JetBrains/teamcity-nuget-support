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
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.tests.agent.PackageSourceImpl;
import jetbrains.buildServer.nuget.tests.mocks.ListMatcher;
import jetbrains.buildServer.util.FileUtil;
import junit.framework.Assert;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static jetbrains.buildServer.agent.BuildFinishedStatus.FINISHED_SUCCESS;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 14:12
 */
public class NuGetAuthorizeFactoryTest extends NuGetActionFactoryTestCase {

  @Test
  public void testAuthorize() throws IOException, RunBuildException {
    final List<PackageSource> sources = Arrays.<PackageSource>asList(
            new PackageSourceImpl("sourc2e", "u2er", "pas2sword")
    );

    final AtomicReference<String> myText = new AtomicReference<String>();

    m.checking(new Expectations(){{
      oneOf(myProcessFactory).executeCommandLine(
              with(equal(ctx)),
              with(equal(myNuGetRunnerPath.getPath())),
              with((Matcher<Collection<String>>)new ListMatcher<String>(Arrays.asList(
                      equal(myNuGetPath.getPath()),
                      equal("TeamCity.AuthorizeFeed"),
                      new BaseMatcher<String>() {
                        public boolean matches(Object o) {
                          try {
                            myText.set(new String(FileUtil.loadFileText(new File((String)o))));
                          } catch (IOException e) {
                            throw new RuntimeException(e);
                          }
                          return true;
                        }

                        public void describeTo(Description description) {
                          description.appendText("any string");
                        }
                      }))
              ),
              with(equal(myWorkDir)),
              with(equal(Collections.<String, String>emptyMap()))
      );
      will(returnValue(createMockBuildProcess("run")));
    }});


    assertRunSuccessfully(i.createAuthenticateFeeds(ctx, sources, nugetParams), FINISHED_SUCCESS);
    assertExecutedMockProcesses("run");

    Assert.assertTrue(myText.get().contains("sourc2e"));
    Assert.assertTrue(myText.get().contains("u2er"));
    Assert.assertTrue(myText.get().contains("pas2sword"));
    m.assertIsSatisfied();
  }

}
