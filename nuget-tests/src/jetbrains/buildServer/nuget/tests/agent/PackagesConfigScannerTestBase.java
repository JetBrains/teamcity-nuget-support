

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolver;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolverImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesConfigScanner;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.FileUtil;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 15:52
 */
public abstract class PackagesConfigScannerTestBase extends BaseTestCase {
  private Mockery m;
  private BuildProgressLogger myLogger;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    myLogger = m.mock(BuildProgressLogger.class);
    m.checking(new Expectations(){{
      allowing(myLogger).message(with(any(String.class)));
    }});
  }

  protected void doTest(String testDataZip, String slnRelPath, String... expectedFiles) throws IOException, RunBuildException {
    final File solution = Paths.getTestDataPath(testDataZip);
    final File dir = createTempDir();
    Assert.assertTrue(ArchiveUtil.unpackZip(solution, "", dir));

    final File sln = new File(dir, slnRelPath);
    Assert.assertTrue(sln.isFile());

    final RepositoryPathResolver res = new RepositoryPathResolverImpl();
    final Collection<File> scannedFiles = new ArrayList<File>();
    for (PackagesConfigScanner scannedFile : createScanner()) {
      scannedFiles.addAll(scannedFile.scanResourceConfig(myLogger, sln, res.resolveRepositoryPath(myLogger, sln, dir)));
    }

    final Collection<File> projects = new TreeSet<File>();
    for (String path : expectedFiles) {
      projects.add(FileUtil.resolvePath(sln.getParentFile(), path));
    }

    Assert.assertEquals(projects, new TreeSet<File>(scannedFiles));
  }

  @NotNull
  protected abstract Collection<? extends PackagesConfigScanner> createScanner();
}
