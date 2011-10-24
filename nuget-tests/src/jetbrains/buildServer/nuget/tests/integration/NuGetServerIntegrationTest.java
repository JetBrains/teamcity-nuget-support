package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.server.exec.impl.NuGetExecutorImpl;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.nuget.server.feed.reader.impl.NuGetFeedReaderImpl;
import jetbrains.buildServer.nuget.server.feed.reader.impl.PackagesFeedParserImpl;
import jetbrains.buildServer.nuget.server.feed.reader.impl.Param;
import jetbrains.buildServer.nuget.server.feed.reader.impl.UrlResolverImpl;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import jetbrains.buildServer.nuget.server.feed.server.controllers.PackageInfoSerializer;
import jetbrains.buildServer.nuget.server.feed.server.controllers.PackageWriterImpl;
import jetbrains.buildServer.nuget.server.feed.server.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.nuget.server.feed.server.process.NuGetServerRunner;
import jetbrains.buildServer.nuget.server.feed.server.process.NuGetServerUriImpl;
import jetbrains.buildServer.nuget.tests.integration.feed.SimpleHttpServer;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.metadata.ArtifactsMetadataEntry;
import jetbrains.buildServer.util.ExceptionUtil;
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

import static org.apache.http.HttpStatus.SC_OK;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 17:38
 */
public class NuGetServerIntegrationTest extends BaseTestCase {
  private Mockery m;
  private Collection<InputStream> myStreams;
  private NuGetTeamCityProvider myProvider;
  private File myLogsDir;
  private FeedHttpClientHolder nyHttpClient;
  private FeedGetMethodFactory myHttpMethods;
  private NuGetFeedReaderImpl myFeedReader;
  private SimpleHttpServer myHttpServer;
  private NuGetServerRunner myNuGetServer;
  private String myHttpServerUrl;
  private String myNuGetServerUrl;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myStreams = new ArrayList<InputStream>();
    myProvider = m.mock(NuGetTeamCityProvider.class);
    myLogsDir = createTempDir();

    nyHttpClient = new FeedHttpClientHolder();
    myHttpMethods = new FeedGetMethodFactory();
    myFeedReader = new NuGetFeedReaderImpl(nyHttpClient, new UrlResolverImpl(nyHttpClient, myHttpMethods), myHttpMethods, new PackagesFeedParserImpl());

    System.out.println("NuGet server LogsDir = " + myLogsDir);

    m.checking(new Expectations() {{
      allowing(myProvider).getNuGetServerRunnerPath();
      will(returnValue(Paths.getNuGetServerRunnerPath()));
    }});
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    for (InputStream stream : myStreams) {
      FileUtil.close(stream);
    }

    for (File file : myLogsDir.listFiles()) {
      System.out.println("File " + file + ": \r\n" + loadAllText(file));
    }

    if (myHttpServer != null) {
      myHttpServer.stop();
    }

    if (myNuGetServer != null) {
      myNuGetServer.stopServer();
    }
  }


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

  private void startNuGetServer() {
    final NuGetServerRunnerSettings settings = m.mock(NuGetServerRunnerSettings.class);
    m.checking(new Expectations() {{
      allowing(settings).getPackagesControllerUrl(); will(returnValue(myHttpServerUrl));
      allowing(settings).getLogsPath(); will(returnValue(myLogsDir));
    }});

    myNuGetServer = new NuGetServerRunner(settings, new NuGetExecutorImpl(myProvider));
    myNuGetServer.startServer();
    NuGetServerUriImpl uri = new NuGetServerUriImpl(myNuGetServer);

    myNuGetServerUrl = uri.getNuGetFeedBaseUri();
    System.out.println("Created nuget server at: " + myNuGetServerUrl);
  }

  private void startSimpleHttpServer(@NotNull final File responseFile) throws IOException {
    final String prefixPath = "/packages";

    myHttpServer = new SimpleHttpServer() {
      @Override
      protected Response getResponse(String request) {
        if (prefixPath.equals(getRequestPath(request))) {
          return getFileResponse(responseFile, Arrays.asList("Content-Type: text/plain; encoding=UTF-8", "Content-Encoding: utf-8"));
        }
        return super.getResponse(request);
      }
    };
    myHttpServer.start();
    myHttpServerUrl = "http://localhost:" + myHttpServer.getPort() + prefixPath;
    System.out.println("Created http server at: " + myHttpServerUrl);
  }

  private void renderResponseFile(@NotNull final String key,
                                  @NotNull final File responseFile) throws PackageLoadException, IOException {
    final File packageFile = Paths.getTestDataPath("packages/" + key);

    final SBuildType buildType = m.mock(SBuildType.class);
    final BuildsManager buildsManager = m.mock(BuildsManager.class);
    final SFinishedBuild build = m.mock(SFinishedBuild.class);
    final BuildArtifact artifact = m.mock(BuildArtifact.class, packageFile.getPath());
    final ArtifactsMetadataEntry entry = m.mock(ArtifactsMetadataEntry.class);

    m.checking(new Expectations() {{
      allowing(build).getBuildId(); will(returnValue(42L));
      allowing(build).getBuildTypeId();  will(returnValue("bt"));
      allowing(build).getBuildType();  will(returnValue(buildType));
      allowing(build).getBuildTypeName(); will(returnValue("buidldzzz"));
      allowing(build).getFinishDate(); will(returnValue(new Date(1319214849319L)));
      allowing(buildType).getLastChangesFinished(); will(returnValue(null));
      allowing(buildsManager).findBuildInstanceById(42); will(returnValue(build));

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
    m.checking(new Expectations(){{
      allowing(entry).getBuildId(); will(returnValue(build.getBuildId()));
      allowing(entry).getKey(); will(returnValue(key));
      allowing(entry).getMetadata(); will(returnValue(Collections.unmodifiableMap(map)));
    }});

    Writer w = new OutputStreamWriter(new FileOutputStream(responseFile), "utf-8");
    w.append("                 ");
    new PackageWriterImpl(buildsManager, new PackageInfoSerializer()).serializePackage(entry, w);
    w.append("                  ");
    FileUtil.close(w);
    System.out.println("Generated response file: " + responseFile);

    String text = loadAllText(responseFile);
    System.out.println("Generated server response:\r\n" + text);
  }

  private String loadAllText(File temp) throws IOException {
    return new String(FileUtil.loadFileText(temp, "utf-8"));
  }

  private Runnable assert200(@NotNull final String req,
                             @NotNull final NameValuePair... reqs) throws IOException {
    return new Runnable() {
      public void run() {
        final HttpGet get = myHttpMethods.createGet(myNuGetServerUrl + req, reqs);
        try {
          final HttpResponse execute = nyHttpClient.execute(get);
          final HttpEntity entity = execute.getEntity();
          System.out.println("Request: " + get.getRequestLine());
          entity.writeTo(System.out);
          System.out.println();
          System.out.println();

          Assert.assertTrue(execute.getStatusLine().getStatusCode() == SC_OK);
        } catch (IOException e) {
          ExceptionUtil.rethrowAsRuntimeException(e);
        } finally {
          get.abort();
        }
      }
    };
  }
}
