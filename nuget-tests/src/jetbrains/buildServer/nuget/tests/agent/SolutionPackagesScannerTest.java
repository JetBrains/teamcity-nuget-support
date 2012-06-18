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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.SolutionPackagesScanner;
import jetbrains.buildServer.nuget.agent.util.sln.impl.SolutionParserImpl;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.FileUtil;
import junit.framework.Assert;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 15:23
 */
public class SolutionPackagesScannerTest extends BaseTestCase {
  private Mockery m;
  private BuildProgressLogger myLogger;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myLogger = m.mock(BuildProgressLogger.class);
  }

  @Test
  public void test_dotNuGet() throws IOException, RunBuildException {
    doTest("integration/nuget-nopackages.zip",
            "nuget-nopackages/ConsoleApplication1.sln",
            "packages.config");
  }

  @Test
  public void test_oldSimple() throws IOException, RunBuildException {
    doTest("integration/solution.zip",
            "nuget-proj.sln");
  }

  @Test
  public void test_01() throws IOException, RunBuildException {
    doTest("integration/test-01.zip",
            "sln1-lib.sln",
            "sln1-lib/packages.config",
            "sln1-lib.test/packages.config"
    );
  }

  @Test
  public void test_02() throws IOException, RunBuildException {
    doTest("integration/test-02.zip",
            "ConsoleApplication1/ConsoleApplication1.sln",
            "packages.config",
            "../ConsoleApplication2/packages.config"
    );
  }

  @Test
  public void test_shared() throws IOException, RunBuildException {
    doTest("integration/test-shared-packages.zip",
            "ConsoleApplication1.sln",
            "ConsoleApplication1/packages.config",
            ".nuget/packages.config"
    );
  }

  private void doTest(String testDataZip, String slnRelPath, String... expectedFiles) throws IOException, RunBuildException {
    final File solution = Paths.getTestDataPath(testDataZip);
    final File dir = createTempDir();
    Assert.assertTrue(ArchiveUtil.unpackZip(solution, "", dir));

    final File sln = new File(dir, slnRelPath);
    Assert.assertTrue(sln.isFile());

    SolutionPackagesScanner scaner = new SolutionPackagesScanner(new SolutionParserImpl());
    Collection<File> scannedFiles = scaner.scanResourceConfig(myLogger, sln, new File(sln.getParentFile(), "packages"));

    final Collection<File> projects = new TreeSet<File>();
    for (String path : expectedFiles) {
      projects.add(FileUtil.resolvePath(sln.getParentFile(), path));
    }

    Assert.assertEquals(projects, new TreeSet<File>(scannedFiles));
  }

}
