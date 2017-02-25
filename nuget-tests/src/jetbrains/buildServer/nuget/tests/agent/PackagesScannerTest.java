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

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesConfigScanner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.ResourcesConfigPackagesScanner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.SolutionPackagesScanner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.SolutionWidePackagesConfigScanner;
import jetbrains.buildServer.nuget.agent.util.sln.impl.SolutionParserImpl;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 16:12
 */
public class PackagesScannerTest extends PackagesConfigScannerTestBase {
  @NotNull
  @Override
  protected Collection<? extends PackagesConfigScanner> createScanner() {
    return Arrays.asList(
            new ResourcesConfigPackagesScanner(),
            new SolutionWidePackagesConfigScanner(),
            new SolutionPackagesScanner(new SolutionParserImpl()));
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

  @Test
  public void test_web() throws IOException, RunBuildException {
    doTest("integration/test-web.zip",
            "ClassLibrary1/ClassLibrary1.sln",
            "ClassLibrary1/packages.config",
            "../WS/packages.config"
    );
  }

  @Test
  public void test_web_no_repository() throws IOException, RunBuildException {
    doTest("integration/test-web-noRepository.zip",
            "ClassLibrary1/ClassLibrary1.sln",
            "ClassLibrary1/packages.config",
            "../WS/packages.config"
    );
  }

}
