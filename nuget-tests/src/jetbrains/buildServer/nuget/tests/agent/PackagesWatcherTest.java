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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.PackagesInfoUploader;
import jetbrains.buildServer.nuget.agent.runner.install.impl.PackagesWatcher;
import jetbrains.buildServer.nuget.common.PackageDependenciesStore;
import jetbrains.buildServer.util.EventDispatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 14:10
 */
public class PackagesWatcherTest extends BaseTestCase {
  private Mockery m;
  private ArtifactsWatcher watcher;
  private NuGetPackagesCollector collector;
  private AgentLifeCycleListener multicaster;
  private AgentRunningBuild build;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    watcher = m.mock(ArtifactsWatcher.class);
    build = m.mock(AgentRunningBuild.class);

    final EventDispatcher<AgentLifeCycleListener> events = EventDispatcher.create(AgentLifeCycleListener.class);
    multicaster = events.getMulticaster();

    final NuGetPackagesCollectorImpl nuGetPackagesCollector = new NuGetPackagesCollectorImpl();
    collector = nuGetPackagesCollector;

    new PackagesWatcher(
            events,
            nuGetPackagesCollector,
            new PackagesInfoUploader(watcher, new PackageDependenciesStore())
    );
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
    collector.addDependenyPackage("aaa", "bbb", null);
    multicaster.buildStarted(build);

    Assert.assertTrue(collector.getUsedPackages().getUsedPackages().isEmpty());

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
}
