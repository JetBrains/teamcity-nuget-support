

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.TempFolderProvider;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.nuget.server.exec.*;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackageCommandProcessor;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackagesCommandImpl;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.StringUtil;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:27
 */
public class ListPackagesCommandTest extends BaseTestCase {
  private Mockery m;
  private NuGetExecutor exec;
  private TempFolderProvider tmpFiles;
  private ListPackagesCommand cmd;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    exec = m.mock(NuGetExecutor.class);
    tmpFiles = m.mock(TempFolderProvider.class);
    cmd = new ListPackagesCommandImpl(exec, tmpFiles);
  }

  private <T> void allowCommandLineCall(final T result, final String... cmd) throws NuGetExecutionException, IOException {
    final List<String> list = new ArrayList<String>(Arrays.<String>asList(cmd));
    m.checking(new Expectations(){{
      oneOf(exec).executeNuGet(with(any(File.class)), with(new BaseMatcher<List<String>>() {
        public boolean matches(Object o) {
          final List<String> entries = (List<String>) o;

          if (entries.size() != list.size()) return false;

          final Iterator<String> actual = entries.iterator();
          final Iterator<String> gold = entries.iterator();
          while(actual.hasNext() && gold.hasNext()) {
            final String gN = gold.next();
            final String aN = actual.next();
            if (gN == null && aN == null) return false;
            if (gN != null && !gN.equals(aN)) return false;
          }

          return actual.hasNext() == gold.hasNext();
        }

        public void describeTo(Description description) {
          description.appendText("Expected commandline: " + StringUtil.join(", ", list));
        }
      }), with(Collections.<PackageSource>emptyList()), with(any(NuGetOutputProcessor.class)));
      will(returnValue(result));
      allowing(tmpFiles).getTempDirectory(); will(returnValue(createTempDir()));
    }});
  }

  @Test
  public void test_run_packages() throws NuGetExecutionException, IOException {
    allowCommandLineCall(Collections.emptyMap(), "TeamCity.ListPackages", "-Request", null, "-Response", null);
    cmd.checkForChanges(new File("nuget"), Arrays.asList(new SourcePackageReference("source", "package", "version"),new SourcePackageReference("source2", "package2", "version2")));
  }
}
