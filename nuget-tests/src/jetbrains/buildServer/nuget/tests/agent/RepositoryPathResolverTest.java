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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolver;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolverImpl;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static jetbrains.buildServer.util.FileUtil.writeFileAndReportErrors;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.12.11 18:32
 */
public class RepositoryPathResolverTest extends BaseTestCase {
  private static final String PROJECT_SLN = "project.sln";
  private Mockery m;
  private BuildProgressLogger myLogger;
  private RepositoryPathResolver myResolver;
  private File myHome;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    myLogger = m.mock(BuildProgressLogger.class);
    myHome = createTempDir();
    writeFileAndReportErrors(new File(myHome, PROJECT_SLN), "fake sln file content");
    myResolver = new RepositoryPathResolverImpl();

    m.checking(new Expectations(){{
      oneOf(myLogger).message(with(any(String.class)));
    }});
  }

  @Test
  public void testDefaultPath() throws RunBuildException {
    doResolveTest(PROJECT_SLN, "packages");
  }

  @Test
  public void testResolveWithConfig_01() throws IOException, RunBuildException {
    FileUtil.copy(Paths.getTestDataPath("config/NuGet-01.config"), new File(myHome, "NuGet.Config"));
    doResolveTest(PROJECT_SLN, "../lib");
  }

  @Test
  public void testResolveWithConfig_02() throws IOException, RunBuildException {
    FileUtil.copy(Paths.getTestDataPath("config/NuGet-02.Config"), new File(myHome, "NuGet.Config"));
    doResolveTest(PROJECT_SLN, "packages");
  }

  @Test
  public void testResolveWithConfig_03() throws IOException, RunBuildException {
    FileUtil.copy(Paths.getTestDataPath("config/NuGet-03.Config"), new File(myHome, "NuGet.Config"));
    doResolveTest(PROJECT_SLN, "../lib");
  }

  @Test
  public void testResolveWithConfig_04() throws IOException, RunBuildException {
    FileUtil.copy(Paths.getTestDataPath("config/NuGet-04.Config"), new File(myHome, "NuGet.Config"));
    doResolveTest(PROJECT_SLN, "C:" + File.separator + "myteam" + File.separator + "teampackages");
  }

  @Test
  public void testResolveWithBroken() throws IOException, RunBuildException {
    m.checking(new Expectations(){{
      oneOf(myLogger).warning(with(any(String.class)));
    }});
    FileUtil.writeFileAndReportErrors(new File(myHome, "nuget.config"), "this is a broken xml");
    doResolveTest(PROJECT_SLN, "packages");
  }

  @Test
  public void testConfigLocation_01() throws Exception {
    ArchiveUtil.unpackZip(Paths.getTestDataPath("config/config_location_1.zip"), "", myHome);
    doResolveTest("apps/firstapp/firstapp.sln", "customizedPath");
  }

  @Test
  public void testConfigLocation_02() throws Exception {
    ArchiveUtil.unpackZip(Paths.getTestDataPath("config/config_location_2.zip"), "", myHome);
    doResolveTest("app.sln", ".nuget/customizedPath");
  }

  @Test
  public void testConfigLocation_03() throws Exception {
    ArchiveUtil.unpackZip(Paths.getTestDataPath("config/config_location_3.zip"), "", myHome);
    doResolveTest("app.sln", "customizedPath");
  }

  @Test
  public void testChainingMultipleConfigs() throws Exception {
    ArchiveUtil.unpackZip(Paths.getTestDataPath("config/multiple_configs.zip"), "", myHome);
    doResolveTest("a/b/app.sln", myHome, "root");
    doResolveTest("a/b/app.sln", new File(myHome, "a"), "a/a");
    doResolveTest("a/b/app.sln", new File(myHome, "a/b"), "a/b/sln");
  }

  @Test
  public void testCaseSensitivity_1() throws Exception {
    FileUtil.copy(Paths.getTestDataPath("config/NuGet-case-01.config"), new File(myHome, "NuGet.Config"));
    doResolveTest(PROJECT_SLN, "customizedPath");
  }

  @Test
  public void testCaseSensitivity_2() throws Exception {
    FileUtil.copy(Paths.getTestDataPath("config/NuGet-case-02.config"), new File(myHome, "NuGet.Config"));
    doResolveTest(PROJECT_SLN, "customizedPath");
  }

  @Test
  public void testCaseSensitivity_3() throws Exception {
    FileUtil.copy(Paths.getTestDataPath("config/NuGet-case-03.config"), new File(myHome, "NuGet.Config"));
    doResolveTest(PROJECT_SLN, "customizedPath");
  }

  private void doResolveTest(String slnFilePath, String expectedRepoPath) throws RunBuildException {
    doResolveTest(slnFilePath, myHome, expectedRepoPath);
  }

  private void doResolveTest(String slnFilePath, File workingDirectory, String expectedRepoPath) throws RunBuildException {
    final File actual = myResolver.resolveRepositoryPath(myLogger, new File(myHome, slnFilePath), workingDirectory);
    Assert.assertTrue(actual.exists(), "Resolved file must exist");
    Assert.assertEquals(actual, FileUtil.getCanonicalFile(actual), "should return absolute canonical path");
    Assert.assertEquals(actual, FileUtil.resolvePath(myHome, expectedRepoPath));
  }
}
