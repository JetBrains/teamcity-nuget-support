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
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.tests.agent.factory.NuGetActionFactoryTestCase;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.TestFor;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 16:23
 */
public class NuGetPackActionFactoryTest extends NuGetActionFactoryTestCase {

  private NuGetPackParameters myPackParameters;
  private File myFile;
  private File myNuGet;
  private File myRoot;
  private File myOut;

  private Collection<String> myExcludes;
  private Collection<String> myProperties;
  private Collection<String> myExtra;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myPackParameters = m.mock(NuGetPackParameters.class);

    myFile = createTempFile();
    myNuGet = createTempFile();
    myRoot = createTempDir();
    myOut = createTempDir();


    myExcludes = new ArrayList<String>();
    myProperties = new ArrayList<String>();
    myExtra = new ArrayList<String>();

    m.checking(new Expectations(){{
      allowing(myPackParameters).getNuGetExeFile(); will(returnValue(myNuGet));
      allowing(myPackParameters).getBaseDirectory(); will(returnValue(myRoot));
      allowing(myPackParameters).getOutputDirectory(); will(returnValue(myOut));

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

      allowing(myPackParameters).getVersion(); will(returnValue("45.239.32.12"));
      allowing(myPackParameters).getSpecFiles(); will(returnValue(Arrays.asList(myFile.getPath())));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet.getPath(),
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12")
              , myCheckoutDir,
              Collections.<String, String>emptyMap());
    }});

    i.createPack(ctx, myFile, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test(expectedExceptions = RunBuildException.class)
  public void test_package_noNuGet() throws RunBuildException {
    FileUtil.delete(myNuGet);
    test_package();
  }

  @Test
  public void test_properties() throws RunBuildException {
    myProperties.add("p1=p2");
    myProperties.add("p3=p24");
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(false));
      allowing(myPackParameters).packSymbols(); will(returnValue(false));

      allowing(myPackParameters).getVersion(); will(returnValue("45.239.32.12"));
      allowing(myPackParameters).getSpecFiles(); will(returnValue(Arrays.asList(myFile.getPath())));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet.getPath(),
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "-Properties", "p1=p2", "-Properties", "p3=p24")
              , myCheckoutDir,
              Collections.<String, String>emptyMap());
    }});

    i.createPack(ctx, myFile, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-20067")
  public void test_no_version() throws RunBuildException {
    myProperties.add("p1=p2");
    myProperties.add("p3=p24");
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(false));
      allowing(myPackParameters).packSymbols(); will(returnValue(false));

      allowing(myPackParameters).getVersion(); will(returnValue("  "));
      allowing(myPackParameters).getSpecFiles(); will(returnValue(Arrays.asList(myFile.getPath())));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet.getPath(),
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Properties", "p1=p2", "-Properties", "p3=p24")
              , myCheckoutDir,
              Collections.<String, String>emptyMap());
    }});

    i.createPack(ctx, myFile, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  public void test_custom_commandline() throws RunBuildException {
    myExtra.add("arg1");
    myExtra.add("arg2");
    m.checking(new Expectations() {{
      allowing(myPackParameters).packTool();    will(returnValue(false));
      allowing(myPackParameters).packSymbols();  will(returnValue(false));

      allowing(myPackParameters).getVersion(); will(returnValue("45.239.32.12"));
      allowing(myPackParameters).getSpecFiles(); will(returnValue(Arrays.asList(myFile.getPath())));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet.getPath(),
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "arg1", "arg2")
              , myCheckoutDir,
              Collections.<String, String>emptyMap());
    }});

    i.createPack(ctx, myFile, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  public void test_excludes() throws RunBuildException {
    myExcludes.add("aaa");
    myExcludes.add("d/v/de");
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(false));
      allowing(myPackParameters).packSymbols(); will(returnValue(false));

      allowing(myPackParameters).getVersion(); will(returnValue("45.239.32.12"));
      allowing(myPackParameters).getSpecFiles(); will(returnValue(Arrays.asList(myFile.getPath())));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet.getPath(),
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "-Exclude", "aaa", "-Exclude", "d/v/de")
              , myCheckoutDir,
              Collections.<String, String>emptyMap());
    }});

    i.createPack(ctx, myFile, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  public void test_package_tool() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(true));
      allowing(myPackParameters).packSymbols(); will(returnValue(false));

      allowing(myPackParameters).getVersion(); will(returnValue("45.239.32.12"));
      allowing(myPackParameters).getSpecFiles(); will(returnValue(Arrays.asList(myFile.getPath())));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet.getPath(),
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "-Tool")
              , myCheckoutDir,
              Collections.<String, String>emptyMap());
    }});

    i.createPack(ctx, myFile, myPackParameters);
    m.assertIsSatisfied();
  }

  @Test
  public void test_package_symbols() throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myPackParameters).packTool(); will(returnValue(false));
      allowing(myPackParameters).packSymbols(); will(returnValue(true));

      allowing(myPackParameters).getVersion(); will(returnValue("45.239.32.12"));
      allowing(myPackParameters).getSpecFiles(); will(returnValue(Arrays.asList(myFile.getPath())));

      oneOf(myProcessFactory).executeCommandLine(ctx, myNuGet.getPath(),
              Arrays.asList(
                      "pack", myFile.getPath(), "-OutputDirectory", myOut.getPath(), "-BasePath", myRoot.getPath(), "-Verbose", "-Version", "45.239.32.12", "-Symbols")
              , myCheckoutDir,
              Collections.<String, String>emptyMap());
    }});

    i.createPack(ctx, myFile, myPackParameters);
    m.assertIsSatisfied();
  }

}
