

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorEx;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorImpl;
import jetbrains.buildServer.nuget.agent.dependencies.impl.PackagesInfoUploader;
import jetbrains.buildServer.nuget.agent.dependencies.impl.PackagesWatcher;
import jetbrains.buildServer.nuget.common.PackageDependencies;
import jetbrains.buildServer.nuget.common.PackageDependenciesStore;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.TestFor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.util.EventDispatcher.create;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 14:10
 */
public class PackagesWatcherTest extends BaseTestCase {
  private Mockery m;
  private ArtifactsWatcher watcher;
  private NuGetPackagesCollectorEx collector;
  private AgentLifeCycleListener multicaster;
  private AgentRunningBuild build;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    watcher = m.mock(ArtifactsWatcher.class);
    build = m.mock(AgentRunningBuild.class);

    final EventDispatcher<AgentLifeCycleListener> events = create(AgentLifeCycleListener.class);
    multicaster = events.getMulticaster();

    BuildAgentConfiguration configuration = m.mock(BuildAgentConfiguration.class);
    m.checking(new Expectations() {{
      allowing(configuration).getServerUrl();
      will(returnValue("http://localhost:8080"));
    }});

    final NuGetPackagesCollectorImpl nuGetPackagesCollector = new NuGetPackagesCollectorImpl(configuration);
    collector = nuGetPackagesCollector;

    new PackagesWatcher(
            events,
            nuGetPackagesCollector,
            new PackagesInfoUploader(watcher, new PackageDependenciesStore())
    );
  }

  @Test
  @TestFor(issues = "TW-23402")
  public void test_state_is_clean_auto() throws InvocationTargetException, IllegalAccessException, IOException {
    final String empty = serializePackages();

    final Map<String, Object[]> arguments = new HashMap<String, Object[]>();
    arguments.put("getUsedPackages", null);
    arguments.put("addCreatedPackage", new Object[]{"aaa", "22.3.4"});
    arguments.put("addDependenyPackage", new Object[]{"aaa", "22.3.4", "ff"});
    arguments.put("addPublishedPackage", new Object[]{"aaa", "22.3.4", "ff"});

    for (Method method : NuGetPackagesCollector.class.getMethods()) {
      if (!Modifier.isPublic(method.getModifiers())) continue;
      if (Modifier.isStatic(method.getModifiers())) continue;

      Assert.assertTrue(arguments.containsKey(method.getName()), "Method " + method + " must be covered");
      Object[] paramz = arguments.get(method.getName());
      if (paramz != null) method.invoke(collector, paramz);
    }

    assertNotEmpty();

    final String state1 = serializePackages();
    Assert.assertFalse(state1.equals(empty));
    collector.removeAllPackages();
    final String state2 = serializePackages();
    Assert.assertEquals(state2, empty);

    assertEmpty();
  }

  @Test
  public void test_state_is_clean_after_build() {
    addAllDependencies();
    multicaster.buildFinished(build, BuildFinishedStatus.FINISHED_SUCCESS);

    assertEmpty();
  }

  @Test
  public void test_data_does_not_removed_on_build_stop() {
    addAllDependencies();

    assertNotEmpty();
    multicaster.buildFinished(build, BuildFinishedStatus.FINISHED_SUCCESS);

    assertEmpty();
  }

  @Test
  public void test_upload_packages_in_before_build_finish_no_packages() {
    multicaster.beforeBuildFinish(build, BuildFinishedStatus.FINISHED_FAILED);

    m.assertIsSatisfied();
  }

  @Test
  public void test_upload_packages_in_before_build_finish_with_packages() throws IOException {
    final File tempDir = createTempDir();
    m.checking(new Expectations(){{
      allowing(build).getBuildTempDirectory();  will(returnValue(tempDir));
      oneOf(watcher).addNewArtifactsPath(with(new NuGetUploadPathMatcher(tempDir)));
    }});

    collector.addDependenyPackage("aaa", "1.2.4", null);
    multicaster.beforeBuildFinish(build, BuildFinishedStatus.FINISHED_FAILED);

    m.assertIsSatisfied();
  }

  @Test
  public void test_clean_packages_on_build_start() {
    addAllDependencies();
    multicaster.buildStarted(build);
    assertEmpty();

    m.assertIsSatisfied();
  }

  private static class NuGetUploadPathMatcher extends BaseMatcher<String> {
    private final File myTempDir;

    public NuGetUploadPathMatcher(File tempDir) {
      myTempDir = tempDir;
    }

    public boolean matches(Object o) {
      String path = ((String) o).trim();
      if (!path.startsWith(myTempDir.getPath())) return false;
      return (path.endsWith("/nuget.xml => .teamcity/nuget")
              || path.endsWith("\\nuget.xml => .teamcity/nuget"));
    }

    public void describeTo(Description description) {
    }
  }


  private void addAllDependencies() {
    collector.addCreatedPackage("aaa", "22.3.4");
    collector.addDependenyPackage("aaa", "22.3.4", "x");
    collector.addPublishedPackage("aaa", "22.3.4", "x");
  }

  private void assertEmpty() {
    Assert.assertTrue(collector.getUsedPackages().getCreatedPackages().isEmpty());
    Assert.assertTrue(collector.getUsedPackages().getUsedPackages().isEmpty());
    Assert.assertTrue(collector.getUsedPackages().getPublishedPackages().isEmpty());
  }

  private void assertNotEmpty() {
    Assert.assertFalse(collector.getUsedPackages().getCreatedPackages().isEmpty());
    Assert.assertFalse(collector.getUsedPackages().getUsedPackages().isEmpty());
    Assert.assertFalse(collector.getUsedPackages().getPublishedPackages().isEmpty());
  }

  @NotNull
  private String serializePackages() throws IOException {
    PackageDependenciesStore s = new PackageDependenciesStore();
    final File file = createTempFile();
    s.save(collector.getUsedPackages(), file);
    return new String(FileUtil.loadFileText(file));
  }

}
