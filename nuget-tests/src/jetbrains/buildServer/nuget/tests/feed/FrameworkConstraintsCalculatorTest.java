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

package jetbrains.buildServer.nuget.tests.feed;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.common.index.FrameworkConstraintsCalculator;
import jetbrains.buildServer.nuget.common.index.NuGetPackageStructureAnalyser;
import jetbrains.buildServer.nuget.common.index.NuGetPackageStructureVisitor;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Evgeniy.Koshkin
 */
public class FrameworkConstraintsCalculatorTest extends BaseTestCase {
  private Mockery m;
  private Set<InputStream> myStreams;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myStreams = new HashSet<InputStream>();
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    for (InputStream stream : myStreams) {
      FileUtil.close(stream);
    }
    super.tearDown();
  }

  @Test
  public void testEmptySetOfConstraints() throws Exception {
    assertPackageConstraints(Collections.<String>emptySet(), "packages/noConstraints.nupkg");
  }

  @Test
  public void shouldProcessFrameworkAssemblyReferences() throws Exception {
    assertPackageConstraints(Sets.newHashSet("net40"), "packages/frameworkAssemblyReferences.nupkg");
  }

  @Test
  public void shouldProcessSubfoldersWithValidShortFrameworkName() throws Exception {
    assertPackageConstraints(Sets.newHashSet("net40-full", "net40-client", "net40"), "packages/subfolders.nupkg");
  }

  @Test
  public void shouldNotProcessSubfoldersLocatedNotAtRoot() throws Exception {
    assertPackageConstraints(Collections.<String>emptySet(), "packages/subfoldersNotAtRootLevel.nupkg");
  }

  @Test
  public void shouldProcessSubfoldersLocatedNotAtRootWithRuntime() throws Exception {
    assertPackageConstraints(Sets.newHashSet("net40-full", "net40-client", "net40"), "packages/subfoldersNotAtRootLevelWithRuntime.nupkg");
  }

  @Test
  public void shouldProcessSubfoldersLocatedNotAtRootForOldMatched() throws Exception {
    System.setProperty(FrameworkConstraintsCalculator.MATCH_FROM_ROOT_PROP, "false");
    assertPackageConstraints(Sets.newHashSet("net40-full", "net40-client", "net40"), "packages/subfoldersNotAtRootLevel.nupkg");
  }

  @Test
  public void shouldNotProcessUnknownSubfolders() throws Exception {
    assertPackageConstraints(Collections.<String>emptySet(), "packages/unknownSubfolders.nupkg");
  }

  @Test
  public void testUnrecognizedFramework() throws Exception {
    assertPackageConstraints(Collections.<String>emptySet(), "packages/unrecognizedFramework.nupkg");
  }

  @Test
  public void testUnrecognizedFrameworkUnderLibSubfolder() throws Exception {
    assertPackageConstraints(Sets.newHashSet("unrecognized"), "packages/unrecognizedFrameworkUnderLib.nupkg");
  }

  @Test
  public void testShortFrameworkNamesCaseInsensitivity() throws Exception {
    assertPackageConstraints(Sets.newHashSet("net40"), "packages/frameworkNamesCaseInsensitivity.nupkg");
  }

  @Test
  public void testSubfoldersNamesCaseInsensitivity() throws Exception {
    assertPackageConstraints(Sets.newHashSet("net40-client"), "packages/subfoldersNamesCaseInsensitivity.nupkg");
  }

  @Test
  public void testDependencyGroup() throws Exception {
    assertPackageConstraints(Sets.newHashSet("net45", "win", "wp80"), "packages/dependencyGroup.nupkg");
  }

  private void assertPackageConstraints(Set<String> expectedConstraints, @NotNull String pathToPackage) throws IOException, PackageLoadException {
    final File pkg = Paths.getTestDataPath(pathToPackage);
    Assert.assertTrue(pkg.isFile(), "Package wasn't found on path " + pkg.getAbsolutePath());
    final FrameworkConstraintsCalculator calculator = new FrameworkConstraintsCalculator();
    new NuGetPackageStructureVisitor(Lists.newArrayList(calculator)).visit(new FileInputStream(pkg));
    assertEquals(expectedConstraints, calculator.getPackageConstraints());
  }
}
