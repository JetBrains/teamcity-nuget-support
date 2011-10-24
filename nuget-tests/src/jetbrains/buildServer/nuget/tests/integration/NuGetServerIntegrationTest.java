package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.nuget.server.feed.reader.impl.Param;
import jetbrains.buildServer.nuget.server.feed.server.controllers.PackageInfoSerializer;
import jetbrains.buildServer.nuget.server.feed.server.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Date;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 17:38
 */
public class NuGetServerIntegrationTest extends NuGetServerIntegrationTestBase {

  @Test
  public void testServerFeed() throws IOException, PackageLoadException, InterruptedException {
    enableDebug();

    final String packageId = "CommonServiceLocator";
    final String key = packageId + ".1.0.nupkg";
    final File responseFile = createTempFile();

    renderResponseFile(key, responseFile);
    startSimpleHttpServer(responseFile);
    startNuGetServer();

    assert200("/Packages()").run();
    assert200("/////Packages()").run();
    assert200("/Packages()", new Param("$filter", "Id eq '" + packageId + "'")).run();
    assert200("////Packages()", new Param("$filter", "Id eq '" + packageId + "'")).run();

    Assert.assertTrue(myFeedReader.queryPackageVersions(myNuGetServerUrl + "/", packageId).size() > 0);
  }

  private void renderResponseFile(@NotNull final String key,
                                  @NotNull final File responseFile) throws PackageLoadException, IOException {
    final File packageFile = Paths.getTestDataPath("packages/" + key);

    final SFinishedBuild build = m.mock(SFinishedBuild.class);
    final BuildArtifact artifact = m.mock(BuildArtifact.class, packageFile.getPath());

    m.checking(new Expectations() {{
      allowing(build).getBuildId(); will(returnValue(42L));
      allowing(build).getBuildTypeId();  will(returnValue("bt"));
      allowing(build).getBuildTypeName(); will(returnValue("buidldzzz"));
      allowing(build).getFinishDate(); will(returnValue(new Date(1319214849319L)));

      allowing(artifact).getInputStream();
      will(new CustomAction("open file") {
        public Object invoke(Invocation invocation) throws Throwable {
          final FileInputStream stream = new FileInputStream(packageFile);
          myStreams.add(stream);
          return stream;
        }
      });

      allowing(artifact).getTimestamp(); will(returnValue(packageFile.lastModified()));
      allowing(artifact).getSize(); will(returnValue(packageFile.length()));
      allowing(artifact).getRelativePath(); will(returnValue(packageFile.getPath()));
      allowing(artifact).getName(); will(returnValue(packageFile.getName()));
    }});

    final LocalNuGetPackageItemsFactory factory = new LocalNuGetPackageItemsFactory();
    final Map<String, String> map = factory.loadPackage(artifact);

    Writer w = new OutputStreamWriter(new FileOutputStream(responseFile), "utf-8");
    w.append("                 ");
    new PackageInfoSerializer().serializePackage(map, build, true, w);
    w.append("                 ");
    FileUtil.close(w);
    System.out.println("Generated response file: " + responseFile);

    String text = loadAllText(responseFile);
    System.out.println("Generated server response:\r\n" + text);
  }

}
