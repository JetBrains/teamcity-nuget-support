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

package jetbrains.buildServer.nuget.tests.agent.publish;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.NuGetVersion;
import jetbrains.buildServer.nuget.agent.commands.NuGetVersionCallback;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.runner.impl.AuthStagesBuilder;
import jetbrains.buildServer.nuget.agent.runner.publish.PublishRunnerStagesBuilder;
import jetbrains.buildServer.nuget.agent.runner.publish.PublishStages;
import jetbrains.buildServer.nuget.agent.runner.publish.impl.PublishStagesImpl;
import jetbrains.buildServer.nuget.agent.util.CompositeBuildProcess;
import jetbrains.buildServer.nuget.agent.util.impl.CompositeBuildProcessImpl;
import jetbrains.buildServer.nuget.tests.agent.PackageSourceImpl;
import jetbrains.buildServer.nuget.tests.mocks.StartsWithMatcher;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 25.06.12 17:27
 */
public class PublishRunnerTest extends BuildProcessTestCase {
  private Mockery m;
  private NuGetActionFactory myActionFactory;
  private BuildRunnerContext myContext;
  private AgentRunningBuild myBuild;
  private BuildProgressLogger myLogger;
  private NuGetVersion myVersion;

  private NuGetPublishParameters myParameters;
  private PublishRunnerStagesBuilder myBuilder;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myActionFactory = m.mock(NuGetActionFactory.class);
    myContext = m.mock(BuildRunnerContext.class);
    myBuild = m.mock(AgentRunningBuild.class);
    myLogger = m.mock(BuildProgressLogger.class);
    myVersion = m.mock(NuGetVersion.class);

    m.checking(new Expectations() {{
      allowing(myContext).getBuild();
      will(returnValue(myBuild));
      allowing(myBuild).getBuildLogger();
      will(returnValue(myLogger));
      allowing(myLogger).message(with(any(String.class)));

      allowing(myBuild).getCheckoutDirectory();
      will(returnValue(createTempDir()));
    }});

    myBuilder = new PublishRunnerStagesBuilder(
            new AuthStagesBuilder(myActionFactory),
            myActionFactory);

    myParameters = m.mock(NuGetPublishParameters.class);

    m.checking(new Expectations() {{
      oneOf(myActionFactory).createVersionCheckCommand(
              with(equal(myContext)),
              with(any(NuGetVersionCallback.class)),
              with(equal(myParameters))
      );
      will(doAll(new CustomAction("callback") {
        public Object invoke(Invocation invocation) throws Throwable {
          ((NuGetVersionCallback) invocation.getParameter(1)).onNuGetVersionCompleted(myVersion);
          return createMockBuildProcess("version");
        }
      }));

      allowing(myActionFactory).createPublishedPackageReport(
              with(equal(myContext)),
              with(equal(myParameters)),
              with(any(File.class))
      );
      will(returnValue(createMockBuildProcess("publish")));
    }});
  }


  @Test
  public void testPublish() throws RunBuildException, IOException {
    final File nupkg = createTempFile();
    final Collection<String> packages = Arrays.asList(nupkg.getPath());
    final Collection<PackageSource> sources = Collections.emptyList();

    CompositeBuildProcess bp = basicTest(nupkg, packages, sources);

    assertRunSuccessfully(bp, BuildFinishedStatus.FINISHED_SUCCESS);
    assertExecutedMockProcesses("version", "push", "publish");
  }

  @Test
  public void testPublish_auth() throws RunBuildException, IOException {
    final File nupkg = createTempFile();
    final Collection<String> packages = Arrays.asList(nupkg.getPath());
    final Collection<PackageSource> sources = Arrays.<PackageSource>asList(new PackageSourceImpl("aaa", "bbb", "ccc"));

    m.checking(new Expectations(){{
      allowing(myVersion).supportAuth(); will(returnValue(true));

      oneOf(myActionFactory).createAuthenticateFeeds(myContext, sources, myParameters);
      will(returnValue(createMockBuildProcess("auth")));

    }});

    CompositeBuildProcess bp = basicTest(nupkg, packages, sources);
    assertRunSuccessfully(bp, BuildFinishedStatus.FINISHED_SUCCESS);
    assertExecutedMockProcesses("version", "auth", "push", "publish");
  }


  @Test
  public void testPublish_auth_wrong_version() throws RunBuildException, IOException {
    final File nupkg = createTempFile();
    final Collection<String> packages = Arrays.asList(nupkg.getPath());
    final Collection<PackageSource> sources = Arrays.<PackageSource>asList(new PackageSourceImpl("aaa", "bbb", "ccc"));

    m.checking(new Expectations(){{
      allowing(myVersion).supportAuth(); will(returnValue(false));

      never(myActionFactory).createAuthenticateFeeds(myContext, sources, myParameters);
      will(returnValue(createMockBuildProcess("auth")));

      oneOf(myLogger).warning(with((Matcher<String>)new StartsWithMatcher("Current NuGet version does not support feed authentication parameters")));
    }});

    CompositeBuildProcess bp = basicTest(nupkg, packages, sources);
    assertRunSuccessfully(bp, BuildFinishedStatus.FINISHED_SUCCESS);
    assertExecutedMockProcesses("version", "push", "publish");
  }

  private CompositeBuildProcess basicTest(final File nupkg,
                                          final Collection<String> packages,
                                          final Collection<PackageSource> sources) throws RunBuildException {
    m.checking(new Expectations() {{
      allowing(myParameters).getNuGetPackageSources();
      will(returnValue(sources));
      allowing(myParameters).getFiles();
      will(returnValue(packages));

      oneOf(myActionFactory).createPush(
              myContext,
              myParameters,
              nupkg);
      will(returnValue(createMockBuildProcess("push")));
    }});

    CompositeBuildProcess bp = new CompositeBuildProcessImpl();
    PublishStages is = new PublishStagesImpl(bp);
    myBuilder.buildStages(myContext, is, myParameters);
    return bp;
  }


}
