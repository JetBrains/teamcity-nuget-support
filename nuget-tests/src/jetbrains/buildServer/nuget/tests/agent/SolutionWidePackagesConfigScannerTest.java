

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.SolutionWidePackagesConfigScanner;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.TestFor;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created 07.01.13 12:16
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class SolutionWidePackagesConfigScannerTest extends BaseTestCase {
  private Mockery m;
  private File myHome;
  private File myPackagesOutput;
  private File mySln;
  private BuildProgressLogger myLogger;
  private SolutionWidePackagesConfigScanner myScanner;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = TCJMockUtils.createInstance();
    myHome = createTempDir();
    myPackagesOutput = createTempDir();
    mySln = new File(myHome, "foo.sln");
    myLogger = m.mock(BuildProgressLogger.class);
    myScanner = new SolutionWidePackagesConfigScanner();
  }

  @Test
  @TestFor(issues = "TW-25191")
  public void test_depects_packages_config() throws IOException, RunBuildException {
    final File config = new File(myHome, ".nuget/packages.config");
    writeTextToFile(config, "<xml />");

    m.checking(new Expectations(){{
      allowing(myLogger).message(with(any(String.class)));
    }});

    final Collection<File> files = myScanner.scanResourceConfig(myLogger, mySln, myPackagesOutput);
    Assert.assertEquals(files.size(), 1);
    Assert.assertTrue(files.contains(config));
  }

  @Test
  public void test_depects_no_packages_config() throws IOException, RunBuildException {
    final Collection<File> files = myScanner.scanResourceConfig(myLogger, mySln, myPackagesOutput);
    Assert.assertEquals(files.size(), 0);
  }
}
