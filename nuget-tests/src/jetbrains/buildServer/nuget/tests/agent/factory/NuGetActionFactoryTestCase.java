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

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.impl.CommandFactoryImpl;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetActionFactoryImpl;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetProcessCallbackImpl;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetWorkdirCalculator;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.nuget.common.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 12:01
 */
public class NuGetActionFactoryTestCase extends BuildProcessTestCase {
  protected Mockery m;
  protected CommandlineBuildProcessFactory myProcessFactory;
  protected NuGetActionFactoryImpl i;
  protected BuildRunnerContext ctx;
  protected AgentRunningBuild build;
  protected NuGetFetchParameters nugetParams;
  protected BuildParametersMap myBuildParametersMap;
  protected NuGetTeamCityProvider myProvider;
  protected File myCheckoutDir;
  protected File myWorkDir;
  protected File myNuGetPath;
  protected File myAgentTemp;
  protected File myNuGetRunnerPath;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();

    myWorkDir = createTempDir();
    myCheckoutDir = createTempDir();
    myAgentTemp = createTempDir();
    myNuGetPath = createTempFile();
    myNuGetRunnerPath = createTempFile();

    myProcessFactory = m.mock(CommandlineBuildProcessFactory.class);
    PackageUsages pu = m.mock(PackageUsages.class);
    myProvider = m.mock(NuGetTeamCityProvider.class);
    i = new NuGetActionFactoryImpl(new NuGetProcessCallbackImpl(myProcessFactory, new NuGetWorkdirCalculator()), pu, new CommandFactoryImpl(myProvider));
    ctx = m.mock(BuildRunnerContext.class);
    build = m.mock(AgentRunningBuild.class);
    nugetParams = m.mock(NuGetFetchParameters.class);
    myBuildParametersMap = m.mock(BuildParametersMap.class);

    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetExeFile();  will(returnValue(myNuGetPath));
      allowing(myProvider).getNuGetRunnerPath(); will(returnValue(myNuGetRunnerPath));

      allowing(ctx).getBuild(); will(returnValue(build));
      allowing(ctx).getBuildParameters(); will(returnValue(myBuildParametersMap));
      allowing(build).getCheckoutDirectory(); will(returnValue(myCheckoutDir));
      allowing(build).getAgentTempDirectory(); will(returnValue(myAgentTemp));
      allowing(ctx).getWorkingDirectory(); will(returnValue(myWorkDir));
    }});
  }

}
