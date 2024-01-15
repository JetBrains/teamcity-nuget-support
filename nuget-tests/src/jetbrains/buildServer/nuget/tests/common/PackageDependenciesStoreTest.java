

package jetbrains.buildServer.nuget.tests.common;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.NuGetPackageInfo;
import jetbrains.buildServer.nuget.common.PackageDependencies;
import jetbrains.buildServer.nuget.common.PackageDependenciesStore;
import jetbrains.buildServer.nuget.common.SourcePackageInfo;
import org.jetbrains.annotations.NotNull;
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

    PackageDependencies deps = new PackageDependencies(Arrays.asList(new NuGetPackageInfo("id1", "v1"), new NuGetPackageInfo("id2", "v2")), Collections.<NuGetPackageInfo>emptyList(), Collections.<SourcePackageInfo>emptyList());

    PackageDependencies load = myStore.load(temp);
    assertEquals(deps, load);
  }

  @Test
  public void test_load_v_0_8() throws IOException {
    File temp = createTempFile(V_0_8_DEPENDENCIES);

    PackageDependencies deps = new PackageDependencies(Arrays.asList(new NuGetPackageInfo("id1", "v1"), new NuGetPackageInfo("id2", "v2")), Arrays.asList(new NuGetPackageInfo("qid1", "qv1"), new NuGetPackageInfo("qid2", "qv2")), Collections.<SourcePackageInfo>emptyList());
    PackageDependencies load = myStore.load(temp);
    assertEquals(deps, load);
  }

  @Test
  public void test_load_v_0_8_2() throws IOException {
    File temp = createTempFile(V_0_8_DEPENDENCIES2);

    PackageDependencies deps = new PackageDependencies(
            Arrays.asList(new NuGetPackageInfo("id1", "v1"), new NuGetPackageInfo("id2", "v2")),
            Arrays.asList(new NuGetPackageInfo("qid1", "qv1"), new NuGetPackageInfo("qid2", "qv2")),
            Arrays.asList(new SourcePackageInfo(new NuGetPackageInfo("qid1", "qv1"), "XtZ"), new SourcePackageInfo(new NuGetPackageInfo("qid2", "qv2"), null)));

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

    PackageDependencies deps = new PackageDependencies(Collections.<NuGetPackageInfo>emptyList(), Arrays.asList(new NuGetPackageInfo("id1", "v1"), new NuGetPackageInfo("id2", "v2")), Collections.<SourcePackageInfo>emptyList());
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

    PackageDependencies deps = new PackageDependencies(Arrays.asList(new NuGetPackageInfo("id1", "v1"), new NuGetPackageInfo("id2", "v2")), Collections.<NuGetPackageInfo>emptyList(), Collections.<SourcePackageInfo>emptyList());
    PackageDependencies load = myStore.load(temp);
    assertEquals(deps, load);
  }

  @Test
  public void test_load_empty() throws IOException {
    File temp = createTempFile("<nuget-dependencies>\n" + "</nuget-dependencies>");

    PackageDependencies deps = new PackageDependencies(Collections.<NuGetPackageInfo>emptyList(), Collections.<NuGetPackageInfo>emptyList(), Collections.<SourcePackageInfo>emptyList());
    PackageDependencies load = myStore.load(temp);
    assertEquals(deps, load);
  }

  @Test
  public void test_saveLoad() throws IOException {
    final File tmp = createTempFile();

    List<NuGetPackageInfo> usedPackages = Arrays.asList(new NuGetPackageInfo("id1", "v1"), new NuGetPackageInfo("id2", "v2"));
    List<NuGetPackageInfo> createdPackages = Arrays.asList(new NuGetPackageInfo("qid1", "qv1"), new NuGetPackageInfo("qid2", "qv2"));

    PackageDependencies deps = new PackageDependencies(usedPackages, createdPackages, Collections.<SourcePackageInfo>emptyList());
    myStore.save(deps, tmp);

    dumpFile(tmp);

    PackageDependencies load = myStore.load(tmp);
    assertEquals(deps, load);
  }

  @Test
  public void test_saveLoad_2() throws IOException {
    final File tmp = createTempFile();

    List<NuGetPackageInfo> usedPackages = Arrays.asList(new NuGetPackageInfo("id1", "v1"), new NuGetPackageInfo("id2", "v2"));
    List<NuGetPackageInfo> createdPackages = Arrays.asList(new NuGetPackageInfo("qid1", "qv1"), new NuGetPackageInfo("qid2", "qv2"));
    List<SourcePackageInfo> publishedPackages = Arrays.asList(new SourcePackageInfo(new NuGetPackageInfo("qid1", "qv1"), "XtZ"), new SourcePackageInfo(new NuGetPackageInfo("qid2", "qv2"), null));

    PackageDependencies deps = new PackageDependencies(usedPackages, createdPackages, publishedPackages);
    myStore.save(deps, tmp);

    dumpFile(tmp);

    PackageDependencies load = myStore.load(tmp);
    assertEquals(deps, load);
  }

  private void assertEquals(@NotNull PackageDependencies deps, @NotNull PackageDependencies load) {
    Assert.assertEquals(new TreeSet<NuGetPackageInfo>(load.getUsedPackages()), new TreeSet<NuGetPackageInfo>(deps.getUsedPackages()));
    Assert.assertEquals(new TreeSet<NuGetPackageInfo>(load.getCreatedPackages()), new TreeSet<NuGetPackageInfo>(deps.getCreatedPackages()));
    Assert.assertEquals(new TreeSet<SourcePackageInfo>(load.getPublishedPackages()), new TreeSet<SourcePackageInfo>(deps.getPublishedPackages()));
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

  private static final String V_0_8_DEPENDENCIES2 =
          "<nuget-dependencies>\n" +
                  "  <packages>\n" +
                  "    <package id=\"id1\" version=\"v1\" />\n" +
                  "    <package id=\"id2\" version=\"v2\" />\n" +
                  "  </packages>\n" +
                  "  <created>\n" +
                  "    <package id=\"qid1\" version=\"qv1\" />\n" +
                  "    <package id=\"qid2\" version=\"qv2\" />\n" +
                  "  </created>\n" +
                  "  <published>\n" +
                  "    <package id=\"qid1\" version=\"qv1\" source=\"XtZ\" />\n" +
                  "    <package id=\"qid2\" version=\"qv2\" />\n" +
                  "  </published>\n" +
                  "</nuget-dependencies>\n";
}
