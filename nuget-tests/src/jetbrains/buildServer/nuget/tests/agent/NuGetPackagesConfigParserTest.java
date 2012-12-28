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
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorImpl;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesConfigParser;
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 2:01
 */
public class NuGetPackagesConfigParserTest extends BaseTestCase {

  @Test
  public void test_01() throws IOException {
    doTest("test-01.xml", new PackageInfo("elmah", "1.1"));
  }

  @Test
  public void test_02() throws IOException {
    doTest("test-02.xml", new PackageInfo("elmah", "1.1"));
  }

  @Test
  public void test_03() throws IOException {
    doTest("test-03.xml",
            new PackageInfo("Machine.Specifications", "0.4.13.0"),
            new PackageInfo("NUnit", "2.5.7.10213")
            )    ;
  }

  @Test
  public void test_04() throws IOException {
    doTest("test-04.xml",
            new PackageInfo("EasyHttp", "1.0.6"),
            new PackageInfo("JsonFx", "2.0.1106.2610"),
            new PackageInfo("structuremap", "2.6.2"));
  }

  public void doTest(@NotNull String testData,
                     @NotNull PackageInfo... packages) throws IOException {
    NuGetPackagesConfigParser p = new NuGetPackagesConfigParser();
    NuGetPackagesCollectorImpl i = new NuGetPackagesCollectorImpl();
    p.parseNuGetPackages(Paths.getTestDataPath("config/" + testData), i);

    if (packages.length != i.getUsedPackages().getUsedPackages().size()) {
      System.out.println(i.getUsedPackages());
    }

    Assert.assertEquals(
            new TreeSet<PackageInfo>(i.getUsedPackages().getUsedPackages()),
            new TreeSet<PackageInfo>(Arrays.asList(packages)));
  }
}
