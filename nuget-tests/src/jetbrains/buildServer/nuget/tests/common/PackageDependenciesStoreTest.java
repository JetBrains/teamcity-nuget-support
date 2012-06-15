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

package jetbrains.buildServer.nuget.tests.common;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.PackageDependencies;
import jetbrains.buildServer.nuget.common.PackageDependenciesStore;
import jetbrains.buildServer.nuget.common.PackageInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 13:24
 */
public class PackageDependenciesStoreTest extends BaseTestCase {
  private PackageDependenciesStore myStore;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    myStore = new PackageDependenciesStore();
  }

  @Test
  public void test_load_v_0_5() throws IOException {
    File temp = createTempFile(V_0_5_DEPENDENCIES);

    PackageDependencies deps = new PackageDependencies(Arrays.asList(new PackageInfo("id1", "v1"), new PackageInfo("id2", "v2")), Collections.<PackageInfo>emptyList());

    PackageDependencies load = myStore.load(temp);
    assertEquals(deps, load);
  }

  @Test
  public void test_load_v_0_8() throws IOException {
    File temp = createTempFile(V_0_8_DEPENDENCIES);

    PackageDependencies deps = new PackageDependencies(Arrays.asList(new PackageInfo("id1", "v1"), new PackageInfo("id2", "v2")), Arrays.asList(new PackageInfo("qid1", "qv1"), new PackageInfo("qid2", "qv2")));
    PackageDependencies load = myStore.load(temp);
    assertEquals(deps, load);
  }

  @Test
  public void test_load_created() throws IOException {
    File temp = createTempFile("<nuget-dependencies>\n" +
            "  <created>\n" +
            "    <package id=\"id1\" version=\"v1\" />\n" +
            "    <package id=\"id2\" version=\"v2\" />\n" +
            "  </created>\n" +
            "</nuget-dependencies>");

    PackageDependencies deps = new PackageDependencies(Collections.<PackageInfo>emptyList(), Arrays.asList(new PackageInfo("id1", "v1"), new PackageInfo("id2", "v2")));
    PackageDependencies load = myStore.load(temp);
    assertEquals(deps, load);
  }

  @Test
  public void test_load_used() throws IOException {
    File temp = createTempFile("<nuget-dependencies>\n" +
            "  <packages>\n" +
            "    <package id=\"id1\" version=\"v1\" />\n" +
            "    <package id=\"id2\" version=\"v2\" />\n" +
            "  </packages>\n" +
            "</nuget-dependencies>");

    PackageDependencies deps = new PackageDependencies(Arrays.asList(new PackageInfo("id1", "v1"), new PackageInfo("id2", "v2")), Collections.<PackageInfo>emptyList());
    PackageDependencies load = myStore.load(temp);
    assertEquals(deps, load);
  }

  @Test
  public void test_load_empty() throws IOException {
    File temp = createTempFile("<nuget-dependencies>\n" + "</nuget-dependencies>");

    PackageDependencies deps = new PackageDependencies(Collections.<PackageInfo>emptyList(), Collections.<PackageInfo>emptyList());
    PackageDependencies load = myStore.load(temp);
    assertEquals(deps, load);
  }

  @Test
  public void test_saveLoad() throws IOException {
    final File tmp = createTempFile();

    List<PackageInfo> usedPackages = Arrays.asList(new PackageInfo("id1", "v1"), new PackageInfo("id2", "v2"));
    List<PackageInfo> createdPackages = Arrays.asList(new PackageInfo("qid1", "qv1"), new PackageInfo("qid2", "qv2"));

    PackageDependencies deps = new PackageDependencies(usedPackages, createdPackages);
    myStore.save(deps, tmp);

    dumpFile(tmp);

    PackageDependencies load = myStore.load(tmp);
    assertEquals(deps, load);
  }

  private void assertEquals(PackageDependencies deps, PackageDependencies load) {
    Assert.assertEquals(new TreeSet<PackageInfo>(load.getUsedPackages()), new TreeSet<PackageInfo>(deps.getUsedPackages()));
    Assert.assertEquals(new TreeSet<PackageInfo>(load.getCreatedPackages()), new TreeSet<PackageInfo>(deps.getCreatedPackages()));
  }

  private static final String V_0_5_DEPENDENCIES =
          "<nuget-dependencies>\n" +
                  "  <packages>\n" +
                  "    <package id=\"id1\" version=\"v1\" />\n" +
                  "    <package id=\"id2\" version=\"v2\" />\n" +
                  "  </packages>\n" +
                  "  <sources>\n" +
                  "    <source>source1</source>\n" +
                  "    <source>source2</source>\n" +
                  "  </sources>\n" +
                  "</nuget-dependencies>";

  private static final String V_0_8_DEPENDENCIES =
          "<nuget-dependencies>\n" +
                  "  <packages>\n" +
                  "    <package id=\"id1\" version=\"v1\" />\n" +
                  "    <package id=\"id2\" version=\"v2\" />\n" +
                  "  </packages>\n" +
                  "  <created>\n" +
                  "    <package id=\"qid1\" version=\"qv1\" />\n" +
                  "    <package id=\"qid2\" version=\"qv2\" />\n" +
                  "  </created>\n" +
                  "</nuget-dependencies>";
}
