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
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.agent.runner.pack.PackRunner;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.tests.agent.mock.MockPackParameters;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.TestFor;
import junit.framework.Assert;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static jetbrains.buildServer.agent.BuildFinishedStatus.FINISHED_SUCCESS;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 24.08.11 12:31
 */
public class PackRunnerTest extends BuildProcessTestCase {
  private Mockery m;
  private NuGetActionFactory myActionFactory;
  private PackagesParametersFactory myParametersFactory;
  private SmartDirectoryCleaner myCleaner;
  private AgentRunningBuild myBuild;
  private BuildRunnerContext myContext;
  private MockPackParameters myPackParameters;
  private BuildProcess myProc;
  private BuildProgressLogger myLogger;
  private File myCheckoutDir;
  private Map<String, String> myConfigParameters;
  private ArtifactsWatcher myPublisher;
  private List<List<File>> myDetectedFiles;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    myActionFactory = m.mock(NuGetActionFactory.class);
    myParametersFactory = m.mock(PackagesParametersFactory.class);
    myCleaner = m.mock(SmartDirectoryCleaner.class);
    myBuild = m.mock(AgentRunningBuild.class);
    myContext = m.mock(BuildRunnerContext.class);
    myProc = m.mock(BuildProcess.class);
    myLogger = m.mock(BuildProgressLogger.class);
    myConfigParameters = new TreeMap<String, String>();
    myPublisher = m.mock(ArtifactsWatcher.class);
    myDetectedFiles = new ArrayList<List<File>>();

    myPackParameters = new MockPackParameters();
    myCheckoutDir = createTempDir();
    m.checking(new Expectations(){{
      oneOf(myParametersFactory).loadPackParameters(myContext); will(returnValue(myPackParameters));

      allowing(myBuild).getBuildLogger(); will(returnValue(myLogger));
      allowing(myContext).getBuild(); will(returnValue(myBuild));
      allowing(myBuild).getCheckoutDirectory(); will(returnValue(myCheckoutDir));

      allowing(myLogger).message(with(any(String.class)));
      allowing(myLogger).activityStarted(with(any(String.class)), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityStarted(with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(any(String.class)), with(any(String.class)));

      allowing(myBuild).getBuildId(); will(returnValue(42L));
      allowing(myBuild).getSharedConfigParameters(); will(returnValue(unmodifiableMap(myConfigParameters)));
      allowing(myBuild).addSharedConfigParameter(with(any(String.class)), with(any(String.class)));
      will(new CustomAction("Add config parameter") {
        public Object invoke(Invocation invocation) throws Throwable {
          myConfigParameters.put((String)invocation.getParameter(0), (String)invocation.getParameter(1));
          return null;
        }
      });

      //noinspection unchecked
      allowing(myActionFactory).createCreatedPackagesReport(with(equal(myContext)), with(any(Collection.class)));
      will(new CustomAction("report created packages") {
        public Object invoke(Invocation invocation) throws Throwable {
          @SuppressWarnings("unchecked")
          final Collection<File> detectedFiles = (Collection<File>) invocation.getParameter(1);
          return new BuildProcessBase() {
            @NotNull
            @Override
            protected BuildFinishedStatus waitForImpl() throws RunBuildException {
              myDetectedFiles.add(new ArrayList<File>(detectedFiles));
              return FINISHED_SUCCESS;
            }
          };
        }
      });
    }});
  }

  private void assertReportedFiles(File... files) {
    Assert.assertEquals(myDetectedFiles.size(), 1);
    Assert.assertEquals(new TreeSet<File>(myDetectedFiles.get(0)), new TreeSet<File>(Arrays.asList(files)));
  }

  private PackRunner createRunner() {
    return new PackRunner(myActionFactory, myParametersFactory, myPublisher, myCleaner);
  }

  @Test
  public void test_packRunner_outputDirectory_notCleaned() throws RunBuildException, IOException {
    final File spec = createTempFile();
    m.checking(new Expectations(){{
      myPackParameters.setOutput(createTempDir());
      myPackParameters.setSpecFiles(spec);

      oneOf(myActionFactory).createPack(myContext, spec, myPackParameters); will(returnValue(myProc));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));
    }});

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunner_outputDirectory_notCleaned_artifacts_no_change() throws RunBuildException, IOException {
    final File spec = createTempFile();
    final File outputDir = createTempDir();
    FileUtil.writeFileAndReportErrors(new File(outputDir, "foo.nupkg"), "zzz");

    m.checking(new Expectations(){{
      myPackParameters.setOutput(outputDir);
      myPackParameters.setSpecFiles(spec);
      myPackParameters.setPublishArtifacts(true);

      oneOf(myActionFactory).createPack(myContext, spec, myPackParameters); will(returnValue(myProc));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myLogger).warning(with(any(String.class)));
    }});

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunner_outputDirectory_notCleaned_artifacts_change() throws RunBuildException, IOException {
    final File spec = createTempFile();
    final File outputDir = createTempDir();

    final File bar = new File(outputDir, "bar.nupkg");
    final File foo = new File(outputDir, "foo.nupkg");

    FileUtil.writeFile(foo, "zzz");
    m.checking(new Expectations(){{
      myPackParameters.setOutput(outputDir);
      myPackParameters.setSpecFiles(spec);
      myPackParameters.setPublishArtifacts(true);
      oneOf(myActionFactory).createPack(myContext, spec, myPackParameters); will(returnValue(myProc));

      oneOf(myProc).start(); will(new CustomAction("create/update fake package") {
        public Object invoke(Invocation invocation) throws Throwable {
          FileUtil.writeFile(bar, "zUUz");
          FileUtil.writeFile(foo, "zsssUUz");
          return null;
        }
      });
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));
      oneOf(myPublisher).addNewArtifactsPath(bar + " => .\r\n" + foo + " => .\r\n");

      allowing(myBuild).isPersonal(); will(returnValue(false));
    }});

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    assertReportedFiles(foo, bar);
    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunner_outputDirectory_notCleaned_artifacts_change_personal() throws RunBuildException, IOException {
    final File spec = createTempFile();
    final File outputDir = createTempDir();

    final File bar = new File(outputDir, "bar.nupkg");
    final File foo = new File(outputDir, "foo.nupkg");

    FileUtil.writeFileAndReportErrors(foo, "zzz");
    m.checking(new Expectations(){{
      myPackParameters.setOutput(outputDir);
      myPackParameters.setSpecFiles(spec);
      myPackParameters.setPublishArtifacts(true);
      oneOf(myActionFactory).createPack(myContext, spec, myPackParameters); will(returnValue(myProc));

      oneOf(myProc).start(); will(new CustomAction("create/update fake package") {
        public Object invoke(Invocation invocation) throws Throwable {
          FileUtil.writeFile(bar, "zUUz");
          FileUtil.writeFile(foo, "zsssUUz");
          return null;
        }
      });
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));
      oneOf(myPublisher).addNewArtifactsPath(bar + " => .\r\n" + foo + " => .\r\n");

      allowing(myBuild).isPersonal(); will(returnValue(true));

      oneOf(myLogger).warning("Packages from personal builds are not published to TeamCity NuGet Feed");
    }});

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    assertReportedFiles(foo, bar);
    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunner_outputDirectory_notCleaned_no_artifacts_change() throws RunBuildException, IOException {
    final File spec = createTempFile();
    final File outputDir = createTempDir();

    final File bar = new File(outputDir, "bar.nupkg");
    final File foo = new File(outputDir, "foo.nupkg");

    FileUtil.writeFile(foo, "zzz");
    m.checking(new Expectations(){{
      myPackParameters.setOutput(outputDir);
      myPackParameters.setSpecFiles(spec);
      oneOf(myActionFactory).createPack(myContext, spec, myPackParameters); will(returnValue(myProc));

      oneOf(myProc).start(); will(new CustomAction("create/update fake package") {
        public Object invoke(Invocation invocation) throws Throwable {
          FileUtil.writeFile(bar, "zUUz");
          FileUtil.writeFile(foo, "zsssUUz");
          return null;
        }
      });
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));
    }});

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    assertReportedFiles(foo, bar);
    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunners_outputDirectory_cleaned_once() throws RunBuildException, IOException {
    final File temp = createTempDir();
    final File spec = createTempFile();
    m.checking(new Expectations(){{
      oneOf(myParametersFactory).loadPackParameters(myContext); will(returnValue(myPackParameters));

      myPackParameters.setCleanOutput(true);
      myPackParameters.setOutput(temp);
      myPackParameters.setSpecFiles(spec);

      exactly(2).of(myActionFactory).createPack(myContext, spec, myPackParameters); will(returnValue(myProc));

      exactly(2).of(myProc).start();
      exactly(2).of(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      exactly(2).of(myCleaner).cleanFolder(with(equal(temp)), with(any(SmartDirectoryCleanerCallback.class)));
    }});

    for(int i = 0; i < 2; i++) {
      final PackRunner runner = createRunner();
      final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
      assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);
    }

    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunners_outputDirectory_cleaned_once2() throws RunBuildException, IOException {
    final File temp = createTempDir();
    final File spec = createTempFile();
    final MockPackParameters params2 = new MockPackParameters();

    m.checking(new Expectations(){{
      oneOf(myParametersFactory).loadPackParameters(myContext); will(returnValue(params2));

      myPackParameters.setOutput(temp);
      myPackParameters.setSpecFiles(spec);

      params2.setCleanOutput(true);
      params2.setOutput(temp);
      params2.setSpecFiles(spec);

      oneOf(myActionFactory).createPack(myContext, spec, myPackParameters); will(returnValue(myProc));
      oneOf(myActionFactory).createPack(myContext, spec, params2); will(returnValue(myProc));

      exactly(2).of(myProc).start();
      exactly(2).of(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myCleaner).cleanFolder(with(equal(temp)), with(any(SmartDirectoryCleanerCallback.class)));
    }});

    for(int i = 0; i < 2; i++) {
      final PackRunner runner = createRunner();
      final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
      assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);
    }

    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunners_different_outputDirectory_cleaned() throws RunBuildException, IOException {
    final File temp1 = createTempDir();
    final File temp2 = createTempDir();
    final File spec = createTempFile();
    final MockPackParameters params1 = myPackParameters;
    final MockPackParameters params2 = new MockPackParameters();

    m.checking(new Expectations(){{
      oneOf(myParametersFactory).loadPackParameters(myContext); will(returnValue(params2));

      params1.setCleanOutput(true);
      params1.setOutput(temp1);
      params1.setSpecFiles(spec);


      params2.setCleanOutput(true);
      params2.setOutput(temp2);
      params2.setSpecFiles(spec);

      oneOf(myActionFactory).createPack(myContext, spec, params1); will(returnValue(myProc));
      oneOf(myActionFactory).createPack(myContext, spec, params2); will(returnValue(myProc));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myCleaner).cleanFolder(with(equal(temp1)), with(any(SmartDirectoryCleanerCallback.class)));
      oneOf(myCleaner).cleanFolder(with(equal(temp2)), with(any(SmartDirectoryCleanerCallback.class)));
    }});

    for(int i = 0; i < 2; i++) {
      final PackRunner runner = createRunner();
      final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
      assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);
    }

    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunner_no_files() throws RunBuildException, IOException {
    final File temp = createTempDir();
    myPackParameters.setOutput(temp);

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunException(process, "Failed to find files to create packages matching");

    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunner_no_files_artifacts() throws RunBuildException, IOException {
    final File temp = createTempDir();
    myPackParameters.setOutput(temp);
    myPackParameters.setPublishArtifacts(true);

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunException(process, "Failed to find files to create packages matching");

    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunner_3_files() throws RunBuildException, IOException {
    final File temp = createTempDir();
    final File spec1 = createTempFile();
    final File spec2 = createTempFile();
    final File spec3 = createTempFile();
    m.checking(new Expectations(){{
      myPackParameters.setOutput(temp);
      myPackParameters.setSpecFiles(spec1, spec2, spec3);

      oneOf(myActionFactory).createPack(myContext, spec1, myPackParameters); will(returnValue(myProc));
      oneOf(myActionFactory).createPack(myContext, spec2, myPackParameters); will(returnValue(myProc));
      oneOf(myActionFactory).createPack(myContext, spec3, myPackParameters); will(returnValue(myProc));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));
    }});

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunner_wildcards_files_clean() throws RunBuildException, IOException {
    final File temp = createTempDir();
    final File home = createTempDir();

    final File spec1 = new File(home, "1.nuspec") {{ FileUtil.writeFile(this, "aaa"); }};
    final File spec2 = new File(home, "2.nuspec") {{ FileUtil.writeFile(this, "aaa"); }};
    final File spec3 = new File(home, "3.nuspec") {{ FileUtil.writeFile(this, "aaa"); }};

    m.checking(new Expectations(){{
      myPackParameters.setCleanOutput(true);
      myPackParameters.setOutput(temp);
      myPackParameters.setSpecFiles(home.getPath() + "/*.nuspec");

      oneOf(myActionFactory).createPack(myContext, spec1, myPackParameters); will(returnValue(myProc));
      oneOf(myActionFactory).createPack(myContext, spec2, myPackParameters); will(returnValue(myProc));
      oneOf(myActionFactory).createPack(myContext, spec3, myPackParameters); will(returnValue(myProc));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myCleaner).cleanFolder(with(equal(temp)), with(any(SmartDirectoryCleanerCallback.class)));
    }});

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
    //should not report files as no nuget package files were created
    assertReportedFiles();
  }


  @Test
  @TestFor(issues = "TW-25796")
  public void test_packRunner_prefer_project() throws RunBuildException, IOException {
    final File temp = createTempDir();
    final File home = createTempDir();

    final File spec1 = new File(home, "1.nuspec") {{ FileUtil.writeFileAndReportErrors(this, "aaa"); }};
    final File spec2 = new File(home, "2.nuspec") {{ FileUtil.writeFileAndReportErrors(this, "aaa"); }};
    final File spec3 = new File(home, "3.nuspec") {{ FileUtil.writeFileAndReportErrors(this, "aaa"); }};
    final File spec1Proj = new File(home, "1.csproj") {{ FileUtil.writeFileAndReportErrors(this, "aaa"); }};
    final File spec2Proj = new File(home, "2.vbproj") {{ FileUtil.writeFileAndReportErrors(this, "aaa"); }};

    m.checking(new Expectations(){{
      myPackParameters.setOutput(temp);
      myPackParameters.setSpecFiles(home.getPath() + "/*.nuspec");
      myPackParameters.setPreferProjectFile(true);

      oneOf(myActionFactory).createPack(myContext, spec1Proj, myPackParameters); will(returnValue(myProc));
      oneOf(myActionFactory).createPack(myContext, spec2Proj, myPackParameters); will(returnValue(myProc));
      oneOf(myActionFactory).createPack(myContext, spec3, myPackParameters); will(returnValue(myProc));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));
    }});

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
    //should not report files as no nuget package files were created
    assertReportedFiles();
  }

  @Test
  public void test_packRunner_outputDirectoryCleaned() throws RunBuildException, IOException {
    final File temp = createTempDir();
    final File spec = createTempFile();
    m.checking(new Expectations(){{
      myPackParameters.setCleanOutput(true);
      myPackParameters.setOutput(temp);
      myPackParameters.setSpecFiles(spec);

      oneOf(myActionFactory).createPack(myContext, spec, myPackParameters); will(returnValue(myProc));
      oneOf(myCleaner).cleanFolder(with(equal(temp)), with(any(SmartDirectoryCleanerCallback.class)));

      oneOf(myProc).start();
      oneOf(myProc).waitFor(); will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));
    }});

    FileUtil.delete(temp);
    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunSuccessfully(process, BuildFinishedStatus.FINISHED_SUCCESS);

    Assert.assertTrue(temp.isDirectory());

    m.assertIsSatisfied();
  }

  @Test
  public void test_packRunner_outputDirectoryCleaned_error() throws RunBuildException, IOException {
    final File temp = createTempDir();
    m.checking(new Expectations(){{
      myPackParameters.setCleanOutput(true);
      myPackParameters.setOutput(temp);

      allowing(myLogger).error(with(any(String.class)));

      oneOf(myCleaner).cleanFolder(with(equal(temp)), with(new BaseMatcher<SmartDirectoryCleanerCallback>() {
        public boolean matches(Object o) {
          SmartDirectoryCleanerCallback cb = (SmartDirectoryCleanerCallback) o;
          cb.logFailedToCleanFile(temp);

          return true;
        }

        public void describeTo(Description description) {
          description.appendText("Callback with failure");
        }
      }));
    }});

    final PackRunner runner = createRunner();
    final BuildProcess process = runner.createBuildProcess(myBuild, myContext);
    assertRunException(process, "Failed to clean output directory");

    m.assertIsSatisfied();
  }
}
