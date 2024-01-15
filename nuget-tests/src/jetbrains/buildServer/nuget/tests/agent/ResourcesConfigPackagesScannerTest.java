

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.ResourcesConfigPackagesScanner;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 15:46
 */
public class ResourcesConfigPackagesScannerTest extends PackagesConfigScannerTestBase {
  @NotNull
  @Override
  protected Collection<? extends ResourcesConfigPackagesScanner> createScanner() {
    return Arrays.asList(new ResourcesConfigPackagesScanner());
  }

  @Test
  public void test_dotNuGet() throws IOException, RunBuildException {
    doTest("integration/nuget-nopackages.zip",
            "nuget-nopackages/ConsoleApplication1.sln");
  }

  @Test
  public void test_oldSimple() throws IOException, RunBuildException {
    doTest("integration/solution.zip",
            "nuget-proj.sln");
  }

  @Test
  public void test_01() throws IOException, RunBuildException {
    doTest("integration/test-01.zip",
            "sln1-lib.sln",
            "sln1-lib/packages.config",
            "sln1-lib.test/packages.config"
    );
  }

  @Test
  public void test_02() throws IOException, RunBuildException {
    doTest("integration/test-02.zip",
            "ConsoleApplication1/ConsoleApplication1.sln",
            "packages.config",
            "../ConsoleApplication2/packages.config"
    );
  }

  @Test
  public void test_shared() throws IOException, RunBuildException {
    doTest("integration/test-shared-packages.zip",
            "ConsoleApplication1.sln",
            "ConsoleApplication1/packages.config"
    );
  }

  @Test
  public void test_web() throws IOException, RunBuildException {
    doTest("integration/test-web.zip",
            "ClassLibrary1/ClassLibrary1.sln",
            "ClassLibrary1/packages.config",
            "../WS/packages.config"
    );
  }

}
