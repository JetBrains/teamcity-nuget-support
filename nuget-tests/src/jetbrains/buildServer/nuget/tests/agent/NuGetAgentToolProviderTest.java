/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.nuget.agent.parameters.impl.NuGetAgentToolProvider;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.util.FileUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;

/**
 * Created by Evgeniy.Koshkin.
 */
public class NuGetAgentToolProviderTest extends BaseTestCase {
  private Mockery m;
  private NuGetAgentToolProvider myToolProvider;
  private BundledToolsRegistry myBundledToolsRegistry;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myBundledToolsRegistry = m.mock(BundledToolsRegistry.class);
    myToolProvider = new NuGetAgentToolProvider(myBundledToolsRegistry);
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    m.assertIsSatisfied();
    super.tearDown();
  }

  @Test
  public void testGetPathByNameWillThrowException() throws Exception {
    assertExceptionThrown(new VoidCallable() {
      @Override
      public void call() throws Exception {
        myToolProvider.getPath("some_tool_name");
      }
    }, ToolCannotBeFoundException.class);
  }

  @Test
  public void testResolveDefaultVersion() throws Exception {
    AgentRunningBuild runningBuild = m.mock(AgentRunningBuild.class);
    BuildRunnerContext runnerContext = m.mock(BuildRunnerContext.class);
    BuildParametersMap buildParameters = m.mock(BuildParametersMap.class);
    BundledTool bundledNuGetTool = m.mock(BundledTool.class);
    File result = FileUtil.createTempDirectory("nuget", "tool");
    m.checking(new Expectations(){{
      allowing(runnerContext).getRunnerParameters(); will(returnValue(Collections.singletonMap(PackagesConstants.NUGET_PATH, "?NuGet.CommandLine.DEFAULT")));
      allowing(runnerContext).getBuildParameters(); will(returnValue(buildParameters));
      allowing(runnerContext).getBuild(); will(returnValue(runningBuild));
      allowing(runningBuild).describe(with(any(boolean.class))); will(returnValue("my build"));
      allowing(runningBuild).getSharedConfigParameters(); will(returnValue(Collections.singletonMap("teamcity.tool.NuGet.CommandLine.defaultVersion", "?NuGet.CommandLine.3.3.0")));
      allowing(myBundledToolsRegistry).findTool("NuGet.CommandLine.3.3.0"); will(returnValue(bundledNuGetTool));
      allowing(bundledNuGetTool).getRootPath(); will(returnValue(result));
    }});
    assertEquals(new File(result, "tools/nuget.exe").getAbsolutePath(), myToolProvider.getPath("?NuGet.CommandLine.DEFAULT", runningBuild, runnerContext));
  }

  @Test
  public void testResolveConcreteVersion() throws Exception {
    AgentRunningBuild runningBuild = m.mock(AgentRunningBuild.class);
    BuildRunnerContext runnerContext = m.mock(BuildRunnerContext.class);
    BundledTool bundledNuGetTool = m.mock(BundledTool.class);
    File result = FileUtil.createTempDirectory("nuget", "tool");
    m.checking(new Expectations(){{
      allowing(runnerContext).getRunnerParameters(); will(returnValue(Collections.singletonMap(PackagesConstants.NUGET_PATH, "?NuGet.CommandLine.3.3.0")));
      allowing(runnerContext).getBuild(); will(returnValue(runningBuild));
      allowing(runningBuild).describe(with(any(boolean.class))); will(returnValue("my build"));
      allowing(myBundledToolsRegistry).findTool("NuGet.CommandLine.3.3.0"); will(returnValue(bundledNuGetTool));
      allowing(bundledNuGetTool).getRootPath(); will(returnValue(result));
    }});
    assertEquals(new File(result, "tools/nuget.exe").getAbsolutePath(), myToolProvider.getPath("?NuGet.CommandLine.3.3.0", runningBuild, runnerContext));
  }
}
