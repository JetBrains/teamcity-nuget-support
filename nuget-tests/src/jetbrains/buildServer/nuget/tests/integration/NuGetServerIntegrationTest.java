package jetbrains.buildServer.nuget.tests.integration;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.nuget.server.feed.reader.impl.Param;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 17:38
 */
public class NuGetServerIntegrationTest extends NuGetServerIntegrationTestBase {

  @Test
  public void testOnePackageFeed() throws Exception {
    enableDebug();

    final String packageId = "CommonServiceLocator";
    final File responseFile = createTempFile();

    renderPackagesResponseFile(responseFile, Paths.getTestDataPath("/packages/" + packageId + ".1.0.nupkg"));
    startSimpleHttpServer(responseFile);
    startNuGetServer();

    assert200("/Packages()").run();
    assert200("/////Packages()").run();
    assert200("/Packages()", new Param("$filter", "Id eq '" + packageId + "'")).run();
    assert200("////Packages()", new Param("$filter", "Id eq '" + packageId + "'")).run();

    Assert.assertTrue(myFeedReader.queryPackageVersions(myNuGetServerUrl, packageId).size() > 0);
  }

  @Test
  public void testConcurrency() throws Exception {
    enableDebug();

    final String packageId = "CommonServiceLocator";
    final File responseFile = createTempFile();

    renderPackagesResponseFile(responseFile, Paths.getTestDataPath("/packages/" + packageId + ".1.0.nupkg"));
    startSimpleHttpServer(responseFile);
    startNuGetServer();

    runAsyncAndFailOnException(
            10,
            assert200("/Packages()"),
            assert200("/////Packages()"),
            assert200("/Packages()", new Param("$filter", "Id eq '" + packageId + "'")),
            assert200("////Packages()", new Param("$filter", "Id eq '" + packageId + "'"))
    );
  }

  @Test
  public void testTwoPackagesFeed() throws Exception {
    enableDebug();

    final String packageId_1 = "CommonServiceLocator";
    final String packageId_2 = "NuGet.Core";

    final File responseFile = createTempFile();
    renderPackagesResponseFile(
            responseFile,
            Paths.getTestDataPath("/packages/" + packageId_1 + ".1.0.nupkg"),
            Paths.getTestDataPath("/packages/" + packageId_2 + ".1.5.20902.9026.nupkg")
            );

    startSimpleHttpServer(responseFile);
    startNuGetServer();

    assert200("/Packages()").run();
    assert200("/////Packages()").run();
    assert200("/Packages()", new Param("$filter", "Id eq '" + packageId_1 + "'")).run();
    assert200("////Packages()", new Param("$filter", "Id eq '" + packageId_1 + "'")).run();
    assert200("/Packages()", new Param("$filter", "Id eq '" + packageId_2 + "'")).run();
    assert200("////Packages()", new Param("$filter", "Id eq '" + packageId_2 + "'")).run();

    Assert.assertTrue(myFeedReader.queryPackageVersions(myNuGetServerUrl, packageId_1).size() > 0);
    Assert.assertTrue(myFeedReader.queryPackageVersions(myNuGetServerUrl, packageId_2).size() > 0);
  }

  @Test
  public void testNuGetClientReadsFeed_15() throws Exception{
    enableDebug();

    final String packageId_1 = "CommonServiceLocator";
    final String packageId_2 = "NuGet.Core";

    final File responseFile = createTempFile();
    renderPackagesResponseFile(
            responseFile,
            Paths.getTestDataPath("/packages/" + packageId_1 + ".1.0.nupkg"),
            Paths.getTestDataPath("/packages/" + packageId_2 + ".1.5.20902.9026.nupkg")
            );

    startSimpleHttpServer(responseFile);
    startNuGetServer();


    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(NuGet.NuGet_1_5.getPath().getPath());
    cmd.addParameter("list");
    cmd.addParameter("-Source");
    cmd.addParameter(myNuGetServerUrl);

    final ExecResult exec = SimpleCommandLineProcessRunner.runCommand(cmd, null);
    Assert.assertEquals(exec.getExitCode(), 0);
    final String stdout = exec.getStdout();
    System.out.println(stdout);
    Assert.assertTrue(stdout.contains(packageId_1), stdout);
    Assert.assertTrue(stdout.contains(packageId_2), stdout);
  }

  @Test(dependsOnGroups = "should create http server for artifacts downlaod")
  public void testNuGetClientInstall_15() throws Exception{
    enableDebug();

    final String packageId_1 = "CommonServiceLocator";
    final String packageId_2 = "NuGet.Core";

    final File responseFile = createTempFile();
    renderPackagesResponseFile(
            responseFile,
            Paths.getTestDataPath("/packages/" + packageId_1 + ".1.0.nupkg"),
            Paths.getTestDataPath("/packages/" + packageId_2 + ".1.5.20902.9026.nupkg")
            );

    startSimpleHttpServer(responseFile);
    startNuGetServer();

    final File home = createTempDir();


    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(NuGet.NuGet_1_5.getPath().getPath());
    cmd.setWorkingDirectory(home);
    cmd.addParameter("install");
    cmd.addParameter(packageId_1);
    cmd.addParameter("-Source");
    cmd.addParameter(myNuGetServerUrl);

    final ExecResult exec = SimpleCommandLineProcessRunner.runCommand(cmd, null);
    System.out.println(exec.getStdout());
    System.out.println(exec.getStderr());

    Assert.assertEquals(exec.getExitCode(), 0);

    Assert.assertTrue(new File(home, packageId_1 + ".1.0").isDirectory());
    Assert.assertTrue(new File(home, packageId_1 + ".1.0/" + packageId_1 + ".1.0.nupkg").isFile());
  }

}
