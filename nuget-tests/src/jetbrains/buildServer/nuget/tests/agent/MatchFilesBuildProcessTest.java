

package jetbrains.buildServer.nuget.tests.agent;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.TestNGUtil;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.runner.publish.MatchFilesBuildProcess;
import jetbrains.buildServer.nuget.tests.Strings;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.FileUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static jetbrains.buildServer.nuget.agent.util.MatchFilesBuildProcessBase.Callback;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 20:38
 */
public class MatchFilesBuildProcessTest extends BuildProcessTestCase {
  private Mockery m;

  private MatchFilesBuildProcess match;
  private AgentRunningBuild build;
  private BuildRunnerContext ctx;
  private NuGetPublishParameters params;
  private MatchFilesBuildProcess.Callback cb;
  private File root;
  private List<String> files;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = TCJMockUtils.createInstance();
    files = new ArrayList<String>();
    build = m.mock(AgentRunningBuild.class);
    ctx = m.mock(BuildRunnerContext.class);
    params = m.mock(NuGetPublishParameters.class);
    cb = m.mock(Callback.class);

    match = new MatchFilesBuildProcess(ctx, params, cb);
    root = createTempDir();

    m.checking(new Expectations() {{
      allowing(params).getFiles();
      will(returnValue(files));
      allowing(ctx).getBuild();
      will(returnValue(build));
      allowing(build).getCheckoutDirectory();
      will(returnValue(root));
    }});
  }

  @Test
  public void test_match_relative_file() throws RunBuildException {
    final File dest = new File(root, "aaa.txt");
    FileUtil.writeFile(dest, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
    }});

    files.add("aaa.txt");
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_match_relative_file_worng_symbols() throws RunBuildException {
    final File dest = new File(root, "aaa.txt");
    FileUtil.writeFile(dest, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
    }});

    files.add("aaa.txt");
    files.add(Strings.EXOTIC);
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_match_relative_file_windows_slashes() throws RunBuildException {
    if (!SystemInfo.isWindows) TestNGUtil.skip("Only for Windows");
    final File dest = new File(root, "a/b/c/d/e/aaa.txt");
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
    }});

    files.add("a\\b\\c\\d\\e\\aaa.txt");
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_match_relative_file_slashes() throws RunBuildException {
    if (!SystemInfo.isWindows) TestNGUtil.skip("Only for Windows");
    final File dest = new File(root, "a/b/c/d/e/aaa.txt");
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
    }});

    files.add("a/b/c/d/e/aaa.txt");
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_match_relative_file_wildcard() throws RunBuildException {
    final File dest = new File(root, "q/e/r/t/aaa.txt");
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
    }});

    files.add("**/*.txt");
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_match_relative_file_wildcard3() throws RunBuildException {
    final File dest = new File(root, "q/e/r/t/aaa.txt");
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "some content");

    final File dest2 = new File(root, "q/v/i/k/abbb.txt");
    FileUtil.createParentDirs(dest2);
    FileUtil.writeFile(dest2, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
      oneOf(cb).fileFound(dest2);
    }});

    files.add("**/*a.txt");
    files.add("**/*b.txt");
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_match_relative_file_wildcard2() throws RunBuildException {
    final File dest = new File(root, "q/e/r/t/aaa.txt");
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "some content");

    final File dest2 = new File(root, "q/e/p/t/aaa.txt");
    FileUtil.createParentDirs(dest2);
    FileUtil.writeFile(dest2, "some content");

    final File dest3 = new File(root, "q/e/p/z/bbb.txt");
    FileUtil.createParentDirs(dest3);
    FileUtil.writeFile(dest3, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
      oneOf(cb).fileFound(dest2);
    }});

    files.add("**/a*.txt");
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }


  @Test
  public void test_match_fullPath_file() throws RunBuildException {
    final File dest = new File(root, "aaa.txt");
    FileUtil.writeFile(dest, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
    }});

    files.add(FileUtil.getCanonicalFile(dest).getPath());
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_match_full_file_wildcard3() throws RunBuildException {
    final File dest = new File(root, "q/e/r/t/aaa.txt");
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
    }});

    files.add(dest.getParentFile() + "/*");
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }


  @Test
  public void test_match_fullPath_file2() throws RunBuildException {
    final File dest = new File(root, "a/b/c/aaa.txt");
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "some content");

    m.checking(new Expectations() {{
      oneOf(cb).fileFound(dest);
    }});

    files.add(FileUtil.getCanonicalFile(dest).getPath());
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);

    m.assertIsSatisfied();
  }

  @Test
  public void test_match_no_files() {
    final File dest = new File(root, "aaa.txt");
    FileUtil.writeFile(dest, "some content");

    assertRunException(match, "Failed to find files to create packages matching");

    m.assertIsSatisfied();
  }
}
