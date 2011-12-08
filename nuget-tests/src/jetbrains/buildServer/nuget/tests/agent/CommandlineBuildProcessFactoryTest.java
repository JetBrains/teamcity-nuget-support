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
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProcessFacade;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.nuget.agent.util.impl.CommandlineBuildProcessFactoryImpl;
import jetbrains.buildServer.runner.SimpleRunnerConstants;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 08.12.11 15:34
 */
public class CommandlineBuildProcessFactoryTest extends BaseTestCase {
  private Mockery m;
  private BuildProcessFacade myFacade;
  private AgentRunningBuild myBuild;
  private BuildRunnerContext myRootContext;
  private BuildRunnerContext mySubContext;
  private CommandlineBuildProcessFactory myFactory;
  private BuildProcess myProcess;
  private File myWorkDir;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myWorkDir = createTempDir();
    myFacade = m.mock(BuildProcessFacade.class);
    myRootContext = m.mock(BuildRunnerContext.class, "root-context");
    mySubContext = m.mock(BuildRunnerContext.class, "sub-context");
    myBuild =  m.mock(AgentRunningBuild.class);
    myProcess = m.mock(BuildProcess.class);
    myFactory = new CommandlineBuildProcessFactoryImpl(myFacade);

    m.checking(new Expectations(){{
      oneOf(myFacade).createBuildRunnerContext(myBuild, SimpleRunnerConstants.TYPE, myWorkDir.getPath(), myRootContext);
      will(returnValue(mySubContext));

      allowing(myRootContext).getBuild(); will(returnValue(myBuild));
      allowing(mySubContext).getBuild(); will(returnValue(myBuild));

      oneOf(myFacade).createExecutable(myBuild, mySubContext);
      will(returnValue(myProcess));
    }});
  }

  @Test
  public void testSupportQuotes() throws RunBuildException {
    m.checking(new Expectations(){{
      oneOf(mySubContext).addRunnerParameter(SimpleRunnerConstants.COMMAND_EXECUTABLE, "program");
      oneOf(mySubContext).addRunnerParameter(SimpleRunnerConstants.COMMAND_PARAMETERS, "\" foo \"");
    }});

    myFactory.executeCommandLine(myRootContext, "program", Arrays.asList("\"", "foo", "\""), myWorkDir, Collections.<String, String>emptyMap());

    m.assertIsSatisfied();
  }

  @Test
  public void testSupportQuotes2() throws RunBuildException {
    m.checking(new Expectations(){{
      oneOf(mySubContext).addRunnerParameter(SimpleRunnerConstants.COMMAND_EXECUTABLE, "program");
      oneOf(mySubContext).addRunnerParameter(SimpleRunnerConstants.COMMAND_PARAMETERS, "\" \" foo\" \"");
    }});

    myFactory.executeCommandLine(myRootContext, "program", Arrays.asList("\"", "\" foo\"", "\""), myWorkDir, Collections.<String, String>emptyMap());

    m.assertIsSatisfied();
  }

  @Test
  public void testQuoteArguments() throws RunBuildException {
    m.checking(new Expectations(){{
      oneOf(mySubContext).addRunnerParameter(SimpleRunnerConstants.COMMAND_EXECUTABLE, "program");
      oneOf(mySubContext).addRunnerParameter(SimpleRunnerConstants.COMMAND_PARAMETERS, "\" \"f o o\" \"z e\" \"");
    }});

    myFactory.executeCommandLine(myRootContext, "program", Arrays.asList("\"", "f o o", "z e", "\""), myWorkDir, Collections.<String, String>emptyMap());

    m.assertIsSatisfied();
  }

  @Test
  public void testSupportEnv() throws RunBuildException {
    m.checking(new Expectations(){{
      oneOf(mySubContext).addRunnerParameter(SimpleRunnerConstants.COMMAND_EXECUTABLE, "program");
      oneOf(mySubContext).addRunnerParameter(SimpleRunnerConstants.COMMAND_PARAMETERS, "");
      oneOf(mySubContext).addEnvironmentVariable("a", "b");
    }});

    myFactory.executeCommandLine(myRootContext, "program", Collections.<String>emptyList(), myWorkDir, Collections.singletonMap("a", "b"));

    m.assertIsSatisfied();
  }
}
