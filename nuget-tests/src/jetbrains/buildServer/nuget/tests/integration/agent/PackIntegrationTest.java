/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.io.ZipUtil;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.TestNGUtil;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.SmartDirectoryCleaner;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.nuget.agent.runner.pack.PackRunner;
import jetbrains.buildServer.nuget.tests.agent.mock.MockPackParameters;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.ZipSlipAwareZipInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 12:58
 */
public class PackIntegrationTest extends IntegrationTestBase {
  protected MockPackParameters myPackParameters;
  private SmartDirectoryCleaner myCleaner;
  private ArtifactsWatcher myPublisher;
  private File myOutputDir;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myPackParameters = new MockPackParameters();
    myCleaner = m.mock(SmartDirectoryCleaner.class);
    myPublisher = m.mock(ArtifactsWatcher.class);

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
  public void test_simple_no_version(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    final File spec = new File(myRoot, "SamplePackage3.nuspec");
    FileUtil.copy(getTestDataPath("SamplePackage3.nuspec"), spec);

    callRunner(nuget, false, false, false, null, Arrays.asList(spec.getPath()));

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
    if(!SystemInfo.isWindows) {
      TestNGUtil.skip("nuget pack command works only on Windows");
    }

    ZipUtil.extract(getTestDataPath("solution.zip"), myRoot, null);
    final File spec = new File(myRoot, "nuget-proj/nuget-proj.csproj");

    msbuild(new File(myRoot, "nuget-proj.sln"));

    callRunner(nuget, spec, false, false, false);

    Assert.assertTrue(nupkgs().length == 1, "There should be only one package created");

    final File nupkg = nupkgs()[0];

    ZipSlipAwareZipInputStream zis = new ZipSlipAwareZipInputStream(new BufferedInputStream(new FileInputStream(nupkg)));
    for(ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
      System.out.println(ze.getName());
    }
    zis.close();

    m.assertIsSatisfied();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_vs_solution_tool(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    if(!SystemInfo.isWindows) {
      TestNGUtil.skip("nuget pack command works only on Windows");
    }

    ZipUtil.extract(getTestDataPath("solution.zip"), myRoot, null);
    final File spec = new File(myRoot, "nuget-proj/nuget-proj.csproj");

    msbuild(new File(myRoot, "nuget-proj.sln"));

    callRunner(nuget, spec, true, false, false);

    Assert.assertTrue(nupkgs().length == 1, "There should be only one package created");
    final File nupkg = nupkgs()[0];

    ZipSlipAwareZipInputStream zis = new ZipSlipAwareZipInputStream(new BufferedInputStream(new FileInputStream(nupkg)));
    for(ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
      System.out.println(ze.getName());
    }
    zis.close();

    m.assertIsSatisfied();
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_vs_solution_symbols(@NotNull final NuGet nuget) throws IOException, RunBuildException {
    if(!SystemInfo.isWindows) {
      TestNGUtil.skip("nuget pack command works only on Windows");
    }

    ZipUtil.extract(getTestDataPath("solution.zip"), myRoot, null);
    final File spec = new File(myRoot, "nuget-proj/nuget-proj.csproj");

    msbuild(new File(myRoot, "nuget-proj.sln"));

    callRunner(nuget, spec, false, true, false);

    Assert.assertTrue(nupkgs().length == 1, "There should be only one package created");
    Assert.assertTrue(symbolsNupkgs().length == 1, "There should be only one symbols package created");

    for (String s : myOutputDir.list()) {
      System.out.println("s = " + s);
    }

    ZipSlipAwareZipInputStream zis = new ZipSlipAwareZipInputStream(new BufferedInputStream(new FileInputStream(nupkgs()[0])));
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
    if(SystemInfo.isWindows) {
      cmd.setExePath("cmd.exe");
      cmd.addParameter("/c");
      cmd.addParameter("%SYSTEMROOT%\\Microsoft.NET\\Framework\\v4.0.30319\\msbuild.exe");
      cmd.addParameter(spec.getPath());
      cmd.addParameter("/t:Rebuild");
    }
    else {
      cmd.setExePath("xbuild");
      cmd.addParameter(spec.getPath());
      cmd.addParameter("/t:Rebuild");
    }

    final ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);
    if(SystemInfo.isWindows) {
      Assert.assertEquals(0, result.getExitCode());
    }
  }

  private void callRunner(@NotNull final NuGet nuget, @NotNull final File spec, final boolean packAsTool, final boolean symbols, final boolean cleanOutput) throws RunBuildException {
    callRunner(nuget, packAsTool, symbols, cleanOutput, Arrays.asList(spec.getPath()));
  }

  private void callRunner(final NuGet nuget, final boolean packAsTool, final boolean symbols, final boolean cleanOutput, final List<String> wildcard) throws RunBuildException {
    callRunner(nuget, packAsTool, symbols, cleanOutput, "45.239.32.12", wildcard);
  }

  private void callRunner(final NuGet nuget, final boolean packAsTool, final boolean symbols, final boolean cleanOutput, @Nullable final String version, final List<String> wildcard) throws RunBuildException {
    m.checking(new Expectations(){{
      myPackParameters.setSpecFiles(wildcard);
      myPackParameters.setNuGetExe(nuget.getPath());
      myPackParameters.setBaseDir(myRoot);
      myPackParameters.setVersion(version);
      myPackParameters.setOutput(myOutputDir);
      myPackParameters.setCleanOutput(cleanOutput);
      myPackParameters.setPackTool(packAsTool);
      myPackParameters.setPackSymbols(symbols);
    }});

    final PackRunner runner = new PackRunner(myActionFactory, myParametersFactory, myPublisher, myCleaner);
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
