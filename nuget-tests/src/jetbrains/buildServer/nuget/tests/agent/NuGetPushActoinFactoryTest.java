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
import jetbrains.buildServer.nuget.agent.commands.impl.CommandFactoryImpl;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetActionFactoryImpl;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 16:46
 */
public class NuGetPushActoinFactoryTest extends BaseTestCase {
  private Mockery m;
  private CommandlineBuildProcessFactory myProcessFactory;
  private NuGetActionFactoryImpl i;
  private BuildRunnerContext ctx;
  private NuGetPublishParameters ps;
  private File myFile;
  private File myNuGet;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myProcessFactory = m.mock(CommandlineBuildProcessFactory.class);
    PackageUsages pu = m.mock(PackageUsages.class);
    i = new NuGetActionFactoryImpl(myProcessFactory, pu, new CommandFactoryImpl());
    ctx = m.mock(BuildRunnerContext.class);
    ps = m.mock(NuGetPublishParameters.class);

    myFile = createTempFile();
    myNuGet = createTempFile();
  }

  @Test
  public void test_command_push() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(ps).getNuGetExeFile(); will(returnValue(myNuGet));
      allowing(ps).getApiKey(); will(returnValue("api-key-guid"));
      allowing(ps).getPublishSource(); will(returnValue("push-feed"));
      allowing(ps).getCreateOnly(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet, Arrays.asList("push", myFile.getPath(), "api-key-guid", "-Source", "push-feed"), myFile.getParentFile(),Collections.<String, String>emptyMap()
      );
    }});

    i.createPush(ctx, ps, myFile);

    m.assertIsSatisfied();
  }

  @Test
  public void test_command_push_no_source() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(ps).getNuGetExeFile(); will(returnValue(myNuGet));
      allowing(ps).getApiKey(); will(returnValue("api-key-guid"));
      allowing(ps).getPublishSource(); will(returnValue(null));
      allowing(ps).getCreateOnly(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet, Arrays.asList("push", myFile.getPath(), "api-key-guid"), myFile.getParentFile(), Collections.<String, String>emptyMap());
    }});

    i.createPush(ctx, ps, myFile);

    m.assertIsSatisfied();
  }

  @Test
  public void test_command_push_no_pacakge() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(ps).getNuGetExeFile(); will(returnValue(myNuGet));
      allowing(ps).getApiKey(); will(returnValue("api-key-guid"));
      allowing(ps).getPublishSource(); will(returnValue("push-feed"));
      allowing(ps).getCreateOnly(); will(returnValue(true));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet, Arrays.asList("push", myFile.getPath(), "api-key-guid", "-CreateOnly", "-Source", "push-feed"), myFile.getParentFile(), Collections.<String, String>emptyMap());
    }});

    i.createPush(ctx, ps, myFile);

    m.assertIsSatisfied();
  }

}
