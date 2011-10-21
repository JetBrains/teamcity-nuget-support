package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.server.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetArtifactsMetadataProvider;
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.metadata.ArtifactsMetadataStorageWriter;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 17:38
 */
public class NuGetServerIntegrationTest extends BaseTestCase {
  private Mockery m;
  private Collection<InputStream> myStreams;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myStreams = new ArrayList<InputStream>();
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    for (InputStream stream : myStreams) {
      FileUtil.close(stream);
    }
  }

  @NotNull
  private BuildArtifact artifact(@NotNull final File file) throws IOException {
    final BuildArtifact a = m.mock(BuildArtifact.class, file.getPath());
    m.checking(new Expectations(){{
      allowing(a).getInputStream(); will(new CustomAction("open file") {
        public Object invoke(Invocation invocation) throws Throwable {
          final FileInputStream stream = new FileInputStream(file);
          myStreams.add(stream);
          return stream;
        }
      });
      allowing(a).getTimestamp(); will(returnValue(file.lastModified()));
      allowing(a).getSize(); will(returnValue(file.length()));
      allowing(a).getRelativePath(); will(returnValue(file.getPath()));
      allowing(a).getName(); will(returnValue(file.getName()));
    }});
    return a;
  }


  @Test
  public void testServerFeed() throws IOException, PackageLoadException {
    final File temp = createTempFile();
    final Mockery m = new Mockery();

    final SBuild build = m.mock(SBuild.class);

    m.checking(new Expectations(){{
      allowing(build).getBuildId(); will(returnValue(42L));
    }});

    final LocalNuGetPackageItemsFactory factory = new LocalNuGetPackageItemsFactory();
    final Map<String, String> map = factory.loadPackage(artifact(Paths.getTestDataPath("packages/CommonServiceLocator.1.0.nupkg")));

    Writer w = new OutputStreamWriter(new FileOutputStream(temp), "utf-8");



    NuGetArtifactsMetadataProvider prov = new NuGetArtifactsMetadataProvider(factory);
    prov.generateMedatadata(build, new ArtifactsMetadataStorageWriter() {
      public void addParameters(@NotNull String s, @NotNull Map<String, String> stringStringMap) {

      }
    });





  }



}
