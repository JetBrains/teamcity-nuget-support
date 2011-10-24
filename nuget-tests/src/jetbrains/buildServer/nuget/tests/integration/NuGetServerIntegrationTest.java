package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.server.exec.impl.NuGetExecutorImpl;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.impl.NuGetFeedReaderImpl;
import jetbrains.buildServer.nuget.server.feed.reader.impl.PackagesFeedParserImpl;
import jetbrains.buildServer.nuget.server.feed.reader.impl.Param;
import jetbrains.buildServer.nuget.server.feed.reader.impl.UrlResolverImpl;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import jetbrains.buildServer.nuget.server.feed.server.controllers.PackageWriterImpl;
import jetbrains.buildServer.nuget.server.feed.server.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.nuget.server.feed.server.process.NuGetServerRunner;
import jetbrains.buildServer.nuget.tests.integration.feed.SimpleHttpServer;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.metadata.ArtifactsMetadataEntry;
import jetbrains.buildServer.util.FileUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.*;

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
    m = new Mockery();
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
    final SBuildType buildType = m.mock(SBuildType.class);
    final BuildsManager buildsManager = m.mock(BuildsManager.class);
    final SFinishedBuild build = m.mock(SFinishedBuild.class);

    m.checking(new Expectations(){{
      allowing(build).getBuildId(); will(returnValue(42L));
      allowing(build).getBuildTypeId(); will(returnValue("bt"));
      allowing(build).getBuildType(); will(returnValue(buildType));
      allowing(build).getBuildTypeName(); will(returnValue("buidldzzz"));
      allowing(build).getFinishDate(); will(returnValue(new Date(1319214849319L)));
      allowing(buildType).getLastChangesFinished(); will(returnValue(null));
      allowing(buildsManager).findBuildInstanceById(42); will(returnValue(build));
    }});

    final LocalNuGetPackageItemsFactory factory = new LocalNuGetPackageItemsFactory();
    final String packageId = "CommonServiceLocator";
    final String key = packageId + ".1.0.nupkg";
    final Map<String, String> map = factory.loadPackage(artifact(Paths.getTestDataPath("packages/" + key)));

    final ArtifactsMetadataEntry entry = m.mock(ArtifactsMetadataEntry.class);
    m.checking(new Expectations(){{
      allowing(entry).getBuildId(); will(returnValue(build.getBuildId()));
      allowing(entry).getKey(); will(returnValue(key));
      allowing(entry).getMetadata(); will(returnValue(Collections.unmodifiableMap(map)));
    }});

    final File temp = createTempFile();
    Writer w = new OutputStreamWriter(new FileOutputStream(temp), "utf-8");
    w.append("                 ");
    new PackageWriterImpl(buildsManager).serializePackage(entry, w);
    w.append("                  ");
    FileUtil.close(w);

    System.out.println("Generated response file: " + temp);

    String text = new String(FileUtil.loadFileText(temp, "utf-8"));
    System.out.println("Generated server response:\r\n" + text);


    SimpleHttpServer server = new SimpleHttpServer(){
      @Override
      protected Response getResponse(String request) {
        if ("/packages".equals(getRequestPath(request))) {
          return getFileResponse(temp, Arrays.asList("Content-Type: text/plain; encoding=UTF-8", "Content-Encoding: utf-8"));
        }
        return super.getResponse(request);
      }
    };
    server.start();
    NuGetServerRunner runner = null;
    try {
      final String url = "http://localhost:" + server.getPort() + "/packages";
      final NuGetServerRunnerSettings settings = m.mock(NuGetServerRunnerSettings.class);
      final NuGetTeamCityProvider provider = m.mock(NuGetTeamCityProvider.class);
      final File logsDir = createTempDir();
      m.checking(new Expectations(){{
        allowing(provider).getNuGetServerRunnerPath(); will(returnValue(Paths.getNuGetServerRunnerPath()));
        allowing(settings).getPackagesControllerUrl(); will(returnValue(url));
        allowing(settings).getLogsPath(); will(returnValue(logsDir));
      }}
      );

      runner = new NuGetServerRunner(settings, new NuGetExecutorImpl(provider));
      runner.startServer();

      final String feedUrl = "http://localhost:" + runner.getPort() + "/nuget";
      System.out.println("Created http server at: " + url);
      System.out.println("Created nuget server at: " + feedUrl);

      final FeedHttpClientHolder client = new FeedHttpClientHolder();
      final FeedGetMethodFactory methods = new FeedGetMethodFactory();
      NuGetFeedReaderImpl reader = new NuGetFeedReaderImpl(client, new UrlResolverImpl(client, methods), methods, new PackagesFeedParserImpl());

      dumpRequest(feedUrl, client, methods, "/Packages()");
      dumpRequest(feedUrl, client, methods, "/Packages()",new Param("$filter", "Id eq '" + packageId + "'"));

      final Collection<FeedPackage> packages = reader.queryPackageVersions(feedUrl, packageId);
      Assert.assertTrue(packages.size() > 0);

      System.out.println("Packages: " + packages);

    } finally {
      if (runner != null) {
        runner.stopServer();
      }

      server.stop();
    }
  }

  private void dumpRequest(String feedUrl, FeedHttpClientHolder client, FeedGetMethodFactory methods, String req, NameValuePair... reqs) throws IOException {
    final HttpGet get = methods.createGet(feedUrl + req, reqs);
    try {
      final HttpResponse execute = client.execute(get);
      final HttpEntity entity = execute.getEntity();
      System.out.println("Request: " + get.getRequestLine());
      entity.writeTo(System.out);
      System.out.println();
      System.out.println();
    } finally {
      get.abort();
    }
  }
}
