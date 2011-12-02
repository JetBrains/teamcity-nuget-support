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

package jetbrains.buildServer.nuget.tests.integration;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.util.io.ZipUtil;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.SmartDirectoryCleaner;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.runner.pack.PackRunner;
import jetbrains.buildServer.nuget.agent.runner.pack.PackRunnerOutputDirectoryTrackerImpl;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 12:58
 */
public class PackIntegrationTest extends IntegrationTestBase {
  protected NuGetPackParameters myPackParameters;
  private SmartDirectoryCleaner myCleaner;
  private File myOutputDir;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myPackParameters = m.mock(NuGetPackParameters.class);
    myCleaner = m.mock(SmartDirectoryCleaner.class);

    m.checking(new Expectations(){{
      oneOf(myParametersFactory).loadPackParameters(myContext); will(returnValue(myPackParameters));

      allowing(myLogger).activityStarted(with(equal("pack")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("pack")), with(any(String.class)));

    }});
    myOutputDir = new File(myRoot, "out");
    //noinspection ResultOfMethodCallIgnored
    myOutputDir.mkdirs();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_simple(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    final File spec = new File(myRoot, "SamplePackage.nuspec");
    FileUtil.copy(getTestDataPath("SamplePackage.nuspec"), spec);

    callRunner(nuget, spec, false, false, false);

    Assert.assertTrue(myOutputDir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".nupkg");
      }
    }).length == 1, "There should be only one package created");

    m.assertIsSatisfied();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_miltiple_simple(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    final File spec = new File(myRoot, "SamplePackage.nuspec");
    FileUtil.copy(getTestDataPath("SamplePackage.nuspec"), spec);
    final File spec2 = new File(myRoot, "SamplePackage2.nuspec");
    FileUtil.copy(getTestDataPath("SamplePackage2.nuspec"), spec2);

    callRunner(nuget, false, false, false, Arrays.asList(spec.getPath(), spec2.getPath()));

    Assert.assertTrue(myOutputDir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".nupkg");
      }
    }).length == 2, "There should be only two package created");

    m.assertIsSatisfied();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_vs_solution(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    ZipUtil.extract(getTestDataPath("solution.zip"), myRoot, null);
    final File spec = new File(myRoot, "nuget-proj/nuget-proj.csproj");

    msbuild(new File(myRoot, "nuget-proj.sln"));

    callRunner(nuget, spec, false, false, false);

    Assert.assertTrue(nupkgs().length == 1, "There should be only one package created");

    final File nupkg = nupkgs()[0];

    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(nupkg)));
    for(ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
      System.out.println(ze.getName());
    }
    zis.close();

    m.assertIsSatisfied();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_vs_solution_tool(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    ZipUtil.extract(getTestDataPath("solution.zip"), myRoot, null);
    final File spec = new File(myRoot, "nuget-proj/nuget-proj.csproj");

    msbuild(new File(myRoot, "nuget-proj.sln"));

    callRunner(nuget, spec, true, false, false);

    Assert.assertTrue(nupkgs().length == 1, "There should be only one package created");
    final File nupkg = nupkgs()[0];

    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(nupkg)));
    for(ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
      System.out.println(ze.getName());
    }
    zis.close();

    m.assertIsSatisfied();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_vs_solution_symbols(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    ZipUtil.extract(getTestDataPath("solution.zip"), myRoot, null);
    final File spec = new File(myRoot, "nuget-proj/nuget-proj.csproj");

    msbuild(new File(myRoot, "nuget-proj.sln"));

    callRunner(nuget, spec, false, true, false);

    Assert.assertTrue(nupkgs().length == 1, "There should be only one package created");
    Assert.assertTrue(symbolsNupkgs().length == 1, "There should be only one symbols package created");

    for (String s : myOutputDir.list()) {
      System.out.println("s = " + s);
    }

    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(nupkgs()[0])));
    for(ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
      System.out.println(ze.getName());
    }

    m.assertIsSatisfied();
  }

  private File[] nupkgs() {
    final File[] files = myOutputDir.listFiles(NUPKG);
    for (File file : files) {
      System.out.println("Found create file: " + file);
    }
    return files;
  }

  private File[] symbolsNupkgs() {
    final File[] files = myOutputDir.listFiles(SYMBOLS_NUPKG);
    for (File file : files) {
      System.out.println("Found create file: " + file);
    }
    return files;
  }

  private void msbuild(File spec) {
    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath("cmd.exe");
    cmd.addParameter("/c");
    cmd.addParameter("%SYSTEMROOT%\\Microsoft.NET\\Framework\\v4.0.30319\\msbuild.exe");
    cmd.addParameter(spec.getPath());
    cmd.addParameter("/t:Rebuild");

    final ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);
    Assert.assertEquals(0, result.getExitCode());
  }

  private void callRunner(@NotNull final NuGet nuget, @NotNull final File spec, final boolean packAsTool, final boolean symbols, final boolean cleanOutput) throws RunBuildException {
    callRunner(nuget, packAsTool, symbols, cleanOutput, Arrays.asList(spec.getPath()));
  }

  private void callRunner(final NuGet nuget, final boolean packAsTool, final boolean symbols, final boolean cleanOutput, final List<String> wildcard) throws RunBuildException {
    m.checking(new Expectations(){{
      allowing(myPackParameters).getCustomCommandline(); will(returnValue(Collections.<String>emptyList()));
      allowing(myPackParameters).getProperties(); will(returnValue(Collections.<String>emptyList()));
      allowing(myPackParameters).getSpecFiles(); will(returnValue(wildcard));
      allowing(myPackParameters).getNuGetExeFile(); will(returnValue(nuget.getPath()));
      allowing(myPackParameters).getBaseDirectory(); will(returnValue(myRoot));
      allowing(myPackParameters).getExclude(); will(returnValue(Collections.<String>emptyList()));
      allowing(myPackParameters).getVersion(); will(returnValue("45.239.32.12"));
      allowing(myPackParameters).getOutputDirectory(); will(returnValue(myOutputDir));
      allowing(myPackParameters).cleanOutputDirectory(); will(returnValue(cleanOutput));

      allowing(myPackParameters).packTool(); will(returnValue(packAsTool));
      allowing(myPackParameters).packSymbols(); will(returnValue(symbols));
    }});

    final PackRunner runner = new PackRunner(myActionFactory, myParametersFactory, new PackRunnerOutputDirectoryTrackerImpl(), myCleaner);
    final BuildProcess proc = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);
  }


  private static FilenameFilter NUPKG = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".nupkg") && !SYMBOLS_NUPKG.accept(dir, name);
      }
    };

  private static FilenameFilter SYMBOLS_NUPKG = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".symbols.nupkg");
      }
    };
}
