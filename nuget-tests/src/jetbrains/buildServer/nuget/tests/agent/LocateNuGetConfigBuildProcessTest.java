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

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.install.LocateNuGetConfigBuildProcess;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import jetbrains.buildServer.util.FileUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 20:49
 */
public class LocateNuGetConfigBuildProcessTest extends BuildProcessTestCase {
  private File myRoot;
  private Mockery m;
  private BuildProgressLogger log;
  private PackagesInstallParameters ps;
  private LocateNuGetConfigBuildProcess.Callback cb;
  private LocateNuGetConfigBuildProcess proc;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myRoot = createTempDir();
    m = new Mockery();
    log = m.mock(BuildProgressLogger.class);
    ps = m.mock(PackagesInstallParameters.class);
    cb = m.mock(LocateNuGetConfigBuildProcess.Callback.class);
    proc = new LocateNuGetConfigBuildProcess(ps, log, cb);
  }

  @Test
  public void test_no_solutionFile() throws RunBuildException {
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(new File(myRoot, "foo.sln")));
    }});

    assertRunException(proc, "Failed to find");
    m.assertIsSatisfied();
  }

  @Test
  public void test_only_solutionFile() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));
    }});

    assertRunException(proc, "Failed to find repositories.config");
    m.assertIsSatisfied();
  }

  @Test
  public void test_solutionFile_packages_empty() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");
    final File packages = new File(sln, "../packages");
    packages.mkdirs();

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));
    }});

    assertRunException(proc, "Failed to find repositories.config");
    m.assertIsSatisfied();
  }

  @Test
  public void test_solutionFile_repositories_config_empty() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");
    final File packages = new File(sln.getParentFile(), "packages");
    packages.mkdirs();
    final File repositories = new File(packages, "repositories.config");

    FileUtil.writeFile(repositories, "<foo />");

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));

      allowing(log).message(with(new StartsWithMatcher("Found packages folder: ")));
      allowing(log).message(with(new StartsWithMatcher("Found list of packages.config files: ")));
      allowing(log).warning(with(new StartsWithMatcher("No packages.config files were found under solution.")));
    }});

    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);
    m.assertIsSatisfied();
  }
  @Test
  public void test_solutionFile_repositories_config_empty2() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");
    final File packages = new File(sln.getParentFile(), "packages");
    packages.mkdirs();
    final File repositories = new File(packages, "repositories.config");

    FileUtil.writeFile(repositories, "<foo /");

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));

      allowing(log).message(with(new StartsWithMatcher("Found packages folder: ")));
      allowing(log).message(with(new StartsWithMatcher("Found list of packages.config files: ")));
      allowing(log).warning(with(new StartsWithMatcher("No packages.config files were found under solution.")));
    }});

    assertRunException(proc, "Failed to parse repositories.config at ");
    m.assertIsSatisfied();
  }

  @Test
  public void test_solutionFile_repositories_config_no_packages_config() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");
    final File packages = new File(sln.getParentFile(), "packages");
    packages.mkdirs();
    final File repositories = new File(packages, "repositories.config");

    FileUtil.writeFile(repositories,
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<repositories>\n" +
            "  <repository path=\"..\\Mvc\\packages.config\" />\n" +
            "  <repository path=\"c:\\Mvc2\\packages.config\" />\n" +
            "</repositories>");

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));

      oneOf(cb).onPackagesConfigFound(new File(myRoot, "Mvc/packages.config"), packages);
      oneOf(cb).onPackagesConfigFound(new File("c:\\Mvc2/packages.config"), packages);

      allowing(log).message(with(new StartsWithMatcher("Found packages folder: ")));
      allowing(log).message(with(new StartsWithMatcher("Found list of packages.config files: ")));
      allowing(log).warning(with(new StartsWithMatcher("No packages.config files were found under solution.")));
    }});

    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);
    m.assertIsSatisfied();
  }

}
