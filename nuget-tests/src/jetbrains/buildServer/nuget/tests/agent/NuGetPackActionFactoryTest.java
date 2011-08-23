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
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.impl.CommandFactoryImpl;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetActionFactoryImpl;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 16:23
 */
public class NuGetPackActionFactoryTest extends BaseTestCase {
  private Mockery m;
  private CommandlineBuildProcessFactory myProcessFactory;
  private NuGetActionFactoryImpl i;
  private BuildRunnerContext ctx;
  private AgentRunningBuild build;
  private NuGetPackParameters myPackParameters;
  private File myFile;
  private File myNuGet;
  private File myRoot;
  private File myOut;
  private File myWorkingDir;
  private Collection<String> myExcludes;
  private Collection<String> myProperties;
  private Collection<String> myExtra;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myProcessFactory = m.mock(CommandlineBuildProcessFactory.class);
    PackageUsages pu = m.mock(PackageUsages.class);
    i = new NuGetActionFactoryImpl(myProcessFactory, pu, new CommandFactoryImpl());
    ctx = m.mock(BuildRunnerContext.class);
    myPackParameters = m.mock(NuGetPackParameters.class);
    build = m.mock(AgentRunningBuild.class);

    myFile = createTempFile();
    myNuGet = createTempFile();
    myRoot = createTempDir();
    myOut = createTempDir();
    myWorkingDir = createTempDir();

    myExcludes = new ArrayList<String>();
    myProperties = new ArrayList<String>();
    myExtra = new ArrayList<String>();

    m.checking(new Expectations(){{
      allowing(ctx).getBuild(); will(returnValue(build));
      allowing(build).getCheckoutDirectory(); will(returnValue(myWorkingDir));
      allowing(myPackParameters).getSpecFile(); will(returnValue(myFile));
      allowing(myPackParameters).getNuGetExeFile(); will(returnValue(myNuGet));
      allowing(myPackParameters).getBaseDirectory(); will(returnValue(myRoot));
      allowing(myPackParameters).getOutputDirectory(); will(returnValue(myOut));
      allowing(myPackParameters).getVersion(); will(returnValue("45.239.32.12"));

      allowing(myPackParameters).getCustomCommandline(); will(returnValue(myExtra));
      allowing(myPackParameters).getProperties(); will(returnValue(myProperties));
      allowing(myPackParameters).getExclude(); will(returnValue(myExcludes));
    }});
  }


  @Test
  public void test_package() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(false));
      allowing(myPackParameters).packSymbols(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet,
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12")
              , myWorkingDir);
    }});

    i.createPack(ctx, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  public void test_properties() throws RunBuildException {
    myProperties.add("p1=p2");
    myProperties.add("p3=p24");
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(false));
      allowing(myPackParameters).packSymbols(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet,
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "-Properties", "p1=p2", "-Properties", "p3=p24")
              , myWorkingDir);
    }});

    i.createPack(ctx, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  public void test_custom_commandline() throws RunBuildException {
    myExtra.add("arg1");
    myExtra.add("arg2");
    m.checking(new Expectations() {{
      allowing(myPackParameters).packTool();
      will(returnValue(false));
      allowing(myPackParameters).packSymbols();
      will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet,
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "arg1", "arg2")
              , myWorkingDir);
    }});

    i.createPack(ctx, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  public void test_excludes() throws RunBuildException {
    myExcludes.add("aaa");
    myExcludes.add("d/v/de");
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(false));
      allowing(myPackParameters).packSymbols(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet,
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "-Exclude", "aaa", "-Exclude", "d/v/de")
              , myWorkingDir);
    }});

    i.createPack(ctx, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  public void test_package_tool() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(true));
      allowing(myPackParameters).packSymbols(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet,
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "-Tool")
              , myWorkingDir);
    }});

    i.createPack(ctx, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  public void test_package_symbols() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(false));
      allowing(myPackParameters).packSymbols(); will(returnValue(true));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet,
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "-Symbols")
              , myWorkingDir);
    }});

    i.createPack(ctx, myPackParameters);
    m.assertIsSatisfied();
  }

}
