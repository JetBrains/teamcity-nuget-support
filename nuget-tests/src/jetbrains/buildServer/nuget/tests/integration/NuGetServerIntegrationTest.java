package jetbrains.buildServer.nuget.tests.integration;

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

}
