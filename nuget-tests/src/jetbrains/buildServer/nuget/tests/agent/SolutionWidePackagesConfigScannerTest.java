/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.SolutionWidePackagesConfigScanner;
import jetbrains.buildServer.util.TestFor;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created 07.01.13 12:16
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class SolutionWidePackagesConfigScannerTest extends BaseTestCase {
  private Mockery m;
  private File myHome;
  private File myPackagesOutput;
  private File mySln;
  private BuildProgressLogger myLogger;
  private SolutionWidePackagesConfigScanner myScanner;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();
    myHome = createTempDir();
    myPackagesOutput = createTempDir();
    mySln = new File(myHome, "foo.sln");
    myLogger = m.mock(BuildProgressLogger.class);
    myScanner = new SolutionWidePackagesConfigScanner();
  }

  @Test
  @TestFor(issues = "TW-25191")
  public void test_depects_packages_config() throws IOException, RunBuildException {
    final File config = new File(myHome, ".nuget/packages.config");
    writeTextToFile(config, "<xml />");

    m.checking(new Expectations(){{
      allowing(myLogger).message(with(any(String.class)));
    }});

    final Collection<File> files = myScanner.scanResourceConfig(myLogger, mySln, myPackagesOutput);
    Assert.assertEquals(files.size(), 1);
    Assert.assertTrue(files.contains(config));
  }

  @Test
  public void test_depects_no_packages_config() throws IOException, RunBuildException {
    final Collection<File> files = myScanner.scanResourceConfig(myLogger, mySln, myPackagesOutput);
    Assert.assertEquals(files.size(), 0);
  }
}
