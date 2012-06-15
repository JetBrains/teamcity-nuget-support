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
import jetbrains.buildServer.nuget.agent.util.sln.SolutionFileParser;
import jetbrains.buildServer.nuget.agent.util.sln.impl.SolutionParserImpl;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.FileUtil;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 20:06
 */
public class SolutionParserTest extends BaseTestCase {
  private SolutionFileParser myParser = new SolutionParserImpl();

  @Test
  public void test_vs2008() throws IOException {
    doTest("vs2008.sln", "SystemCoreReferenced\\SystemCoreReferenced.csproj", "SystemCoreNotReferenced\\SystemCoreNotReferenced.csproj");
  }

  @Test
  public void test_vs2010() throws IOException {
    doTest( "vs2010.sln",
            "SystemCoreReferenced/SystemCoreReferenced.csproj",
            "SystemCoreNotReferenced_ImplicitAllowed/SystemCoreNotReferenced_ImplicitAllowed.csproj",
            "SystemCoreNotReferenced_ImplicitNotAllo/SystemCoreNotReferenced_ImplicitNotAllo.csproj",
            "NotDefaultImplicitReference/NotDefaultImplicitReference.csproj");
  }

  @Test
  public void test_vs2010_maxi() throws IOException {
    doTest( "Lunochod1.sln",
            "Lunochod1\\Lunochod1.csproj",
            "Lunochod2\\Lunochod2.vcxproj",
            "Lunochod3\\Lunochod3.vbproj",
            "Lunochod5\\Lunochod5.csproj",
            "Lunochod6\\Lunochod6.csproj",
            "Lunochod6.Tests\\Lunochod6.Tests.csproj"
            );
  }

  @Test
  public void test_webSite() throws IOException {
    doTest( "WebSiteReferencedProjects.sln",
            "..\\..\\WebSites\\WebSite2",
            "ClassLibrary1\\ClassLibrary1.csproj"
            );
  }

  @NotNull
  private File getTestDataPath(@NotNull String path) {
    return Paths.getTestDataPath("sln/" + path);
  }

  private void doTest(String slnName, String... relPaths) throws IOException {
    final File sln = getTestDataPath(slnName);
    final Collection<File> projects = new TreeSet<File>();
    for (String path : relPaths) {
      projects.add(FileUtil.getCanonicalFile(new File(sln.getParent(), path)));
    }

    Assert.assertEquals(projects, new TreeSet<File>(myParser.parseProjectFiles(sln)));
  }

}
