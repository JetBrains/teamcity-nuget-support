

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolverImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.*;
import jetbrains.buildServer.nuget.agent.util.sln.impl.SolutionParserImpl;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.FileUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 20:49
 */
public class LocateNuGetConfigBuildProcessTest extends BuildProcessTestCase {
  private File myRoot;
  private Mockery m;
  private BuildProgressLogger log;
  private NuGetFetchParameters ps;
  private PackagesInstallerCallback cb;
  private LocateNuGetConfigBuildProcess proc;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myRoot = createTempDir();
    m = TCJMockUtils.createInstance();
    log = m.mock(BuildProgressLogger.class);
    ps = m.mock(NuGetFetchParameters.class);
    cb = m.mock(PackagesInstallerCallback.class);
    proc = new LocateNuGetConfigBuildProcess(ps, log, new RepositoryPathResolverImpl(), Arrays.<PackagesConfigScanner>asList(
            new ResourcesConfigPackagesScanner(),
            new SolutionPackagesScanner(new SolutionParserImpl()),
            new SolutionWidePackagesConfigScanner()));
    proc.addInstallStageListener(cb);

    m.checking(new Expectations(){{
      allowing(log).activityStarted(with(any(String.class)),with(any(String.class)), with(any(String.class)));
      allowing(log).activityFinished(with(any(String.class)), with(any(String.class)));
      allowing(log).message(with(any(String.class)));
      allowing(ps).getWorkingDirectory();
      will(returnValue(myRoot));
      allowing(cb).onNoPackagesConfigsFound();
    }});
  }

  @Test
  public void test_no_solutionFile() throws RunBuildException {
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(new File(myRoot, "foo.sln")));
      allowing(log).warning(with(any(String.class)));
    }});

    assertRunException(proc, "Failed to open solution file");
    m.assertIsSatisfied();
  }

  @Test
  public void test_only_solutionFile() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));

      oneOf(cb).onSolutionFileFound(sln, new File(sln.getParentFile(), "packages"));
      allowing(log).warning(with(any(String.class)));
    }});

    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);
    m.assertIsSatisfied();
  }

  @Test
  public void test_solutionFile_packages_empty() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");
    final File packages = new File(sln.getParentFile(), "packages");
    packages.mkdirs();

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));

      oneOf(cb).onSolutionFileFound(sln, packages);

      allowing(log).warning(with(any(String.class)));
    }});

    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);
    m.assertIsSatisfied();
  }

  @Test
  public void test_solutionFile_repositories_config_empty() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");
    final File packages = new File(sln.getParentFile(), "packages");
    packages.mkdirs();
    final File repositories = new File(packages, "repositories.config");

    FileUtil.writeFile(repositories, "<foo />");

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));

      oneOf(cb).onSolutionFileFound(sln, packages);

      allowing(log).message(with(new StartsWithMatcher("Found packages folder: ")));
      allowing(log).message(with(new StartsWithMatcher("Found list of packages.config files: ")));
      allowing(log).warning(with(new StartsWithMatcher("No packages.config files were found under solution.")));
    }});

    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);
    m.assertIsSatisfied();
  }
  @Test
  public void test_solutionFile_repositories_config_empty2() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");
    final File packages = new File(sln.getParentFile(), "packages");
    packages.mkdirs();
    final File repositories = new File(packages, "repositories.config");

    FileUtil.writeFile(repositories, "<foo /");

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));

      oneOf(cb).onSolutionFileFound(sln, packages);

      allowing(log).message(with(new StartsWithMatcher("Found packages folder: ")));
      allowing(log).message(with(new StartsWithMatcher("Found list of packages.config files: ")));
      allowing(log).warning(with(new StartsWithMatcher("No packages.config files were found under solution.")));
    }});

    assertRunException(proc, "Failed to parse repositories.config at ");
    m.assertIsSatisfied();
  }

  @Test
  public void test_solutionFile_repositories_config_no_packages_config() throws RunBuildException {
    final File sln = new File(myRoot, "foo.sln");
    final File packages = new File(sln.getParentFile(), "packages");
    packages.mkdirs();
    final File repositories = new File(packages, "repositories.config");

    FileUtil.writeFile(repositories,
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<repositories>\n" +
            "  <repository path=\"..\\Mvc\\packages.config\" />\n" +
            "  <repository path=\"c:\\Mvc2\\packages.config\" />\n" +
            "</repositories>");

    FileUtil.writeFile(sln, "Fake solution file");
    m.checking(new Expectations() {{
      allowing(ps).getSolutionFile();
      will(returnValue(sln));

      oneOf(cb).onSolutionFileFound(sln, packages);
      allowing(log).message(with(new StartsWithMatcher("Found packages folder: ")));
      allowing(log).message(with(new StartsWithMatcher("Found list of packages.config files: ")));
      allowing(log).warning(with(new StartsWithMatcher("No packages.config files were found under solution.")));
      allowing(log).warning(with(new StartsWithMatcher("Found packages.config file does not exist")));
    }});

    assertRunSuccessfully(proc, BuildFinishedStatus.FINISHED_SUCCESS);
    m.assertIsSatisfied();
  }

}
