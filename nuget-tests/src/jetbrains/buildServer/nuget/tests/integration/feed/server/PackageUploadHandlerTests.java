

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.controllers.MockResponse;
import jetbrains.buildServer.nuget.common.PackageExistsException;
import jetbrains.buildServer.nuget.common.index.NuGetPackageAnalyzer;
import jetbrains.buildServer.nuget.common.index.PackageAnalyzer;
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCacheReset;
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadHandlerContext;
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadMetadataHandler;
import jetbrains.buildServer.nuget.feed.server.controllers.upload.PackageUploadHandler;
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes;
import jetbrains.buildServer.serverSide.RunningBuildEx;
import jetbrains.buildServer.serverSide.RunningBuildsCollection;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.artifacts.limits.ArtifactsUploadLimit;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.FileUtil;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Dmitry.Tretyakov
 * Date: 22.12.2016
 * Time: 16:37
 */
@Test
public class PackageUploadHandlerTests {

  private static final String SERVLET_PATH = "/app/nuget/v1/FeedService.svc";
  private static final String REQUEST_BODY = "--3576595b-8e57-4d70-91bb-701d5aab54ea\r\n" +
    "Content-Type: application/octet-stream\r\n" +
    "Content-Disposition: form-data; name=package; filename=package.nupkg; filename*=utf-8''package.nupkg\r\n" +
    "\r\n" +
    "Hello\r\n" +
    "--3576595b-8e57-4d70-91bb-701d5aab54ea--\r\n";
  private static final NuGetFeedUploadHandlerContext FEED_DATA = new NuGetFeedUploadHandlerContext() {
    @Override
    public String getFeedName() {
      return "TestFeed";
    }
  };

  public void testNonMultipartRequest() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    PackageAnalyzer packageAnalyzer = new NuGetPackageAnalyzer();
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler = m.mock(NuGetFeedUploadMetadataHandler.class);

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler = new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,
      packageAnalyzer, cacheReset, serverSettings, metadataHandler);
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    request.setContentType("form/url-encoded");

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 400);
  }

  public void testUploadWithoutApiKey() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    PackageAnalyzer packageAnalyzer = new NuGetPackageAnalyzer();
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setBody("".getBytes());

    m.checking(new Expectations() {
      {
        oneOf(serverSettings).getRootUrl();
        will(returnValue("http://localhost:8111"));
        oneOf(serverSettings).getArtifactDirectories();
        File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
        will(returnValue(Arrays.asList(tempDirectory)));
      }
    });

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 403);
  }

  public void testUploadWithInvalidApiKey() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    PackageAnalyzer packageAnalyzer = new NuGetPackageAnalyzer();
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);

    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "aaa");
    request.setBody("".getBytes());

    m.checking(new Expectations() {
      {
        oneOf(serverSettings).getRootUrl();
        will(returnValue("http://localhost:8111"));
        oneOf(serverSettings).getArtifactDirectories();
        File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
        will(returnValue(Arrays.asList(tempDirectory)));
      }
    });

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 403);
  }

  public void testUploadWithoutRunningBuild() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    PackageAnalyzer packageAnalyzer = new NuGetPackageAnalyzer();
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(null));
      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody("".getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 403);
    m.assertIsSatisfied();
  }

  public void testUploadInvalidFile() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    PackageAnalyzer packageAnalyzer = new NuGetPackageAnalyzer();
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));

      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody((
      "--3576595b-8e57-4d70-91bb-701d5aab54ea\r\n" +
        "Content-Type: application/octet-stream\r\n" +
        "Content-Disposition: form-data; name=file; filename=file.ext; filename*=utf-8''file.ext\r\n" +
        "\r\n" +
        "Hello\r\n" +
        "--3576595b-8e57-4d70-91bb-701d5aab54ea--\r\n").getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 400);
    m.assertIsSatisfied();
  }

  public void testUploadWithExceedingArtifactLimit() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    PackageAnalyzer packageAnalyzer = new NuGetPackageAnalyzer();
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      oneOf(build).getArtifactsLimit();
      will(returnValue(new ArtifactsUploadLimit(1L, null)));
      oneOf(build).addBuildProblem(with(any(BuildProblemData.class)));

      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 413);
    m.assertIsSatisfied();
  }

  public void testUploadWithExceedingTotalLimit() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    PackageAnalyzer packageAnalyzer = new NuGetPackageAnalyzer();
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      oneOf(build).getArtifactsLimit();
      will(returnValue(new ArtifactsUploadLimit(null, 0L)));
      oneOf(build).addBuildProblem(with(any(BuildProblemData.class)));

      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 413);
    m.assertIsSatisfied();
  }

  public void testUploadWithTotalLimitLessThanArtifact() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    PackageAnalyzer packageAnalyzer = new NuGetPackageAnalyzer();
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      oneOf(build).getArtifactsLimit();
      will(returnValue(new ArtifactsUploadLimit(1000000L, 1L)));
      oneOf(build).addBuildProblem(with(any(BuildProblemData.class)));
      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 413);
    m.assertIsSatisfied();
  }

  public void testUploadInvalidPackage() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    PackageAnalyzer packageAnalyzer = new NuGetPackageAnalyzer();
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      oneOf(build).getArtifactsLimit();
      will(returnValue(ArtifactsUploadLimit.UNLIMITED));

      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 400);
    m.assertIsSatisfied();
  }

  public void testInternalFailureWhileUpload() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    MetadataStorage metadataStorage = m.mock(MetadataStorage.class);
    PackageAnalyzer packageAnalyzer = m.mock(PackageAnalyzer.class);
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);
    Map<String, String> metadata = CollectionsUtil.asMap(
      NuGetPackageAttributes.ID, "Id",
      NuGetPackageAttributes.NORMALIZED_VERSION, "1.0.0");

    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      one(build).getArtifactsLimit();
      will(returnValue(ArtifactsUploadLimit.UNLIMITED));
      oneOf(packageAnalyzer).analyzePackage(with(any(InputStream.class)));
      will(returnValue(metadata));
      oneOf(packageAnalyzer).getSha512Hash(with(any(InputStream.class)));
      will(returnValue("hash"));
      oneOf(build).getBuildTypeId();
      will(returnValue("type"));
      oneOf(build).getBuildId();
      will(returnValue(3641L));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      one(build).getBuildOwnParameters();
      will(returnValue(Collections.emptyMap()));
      oneOf(build).publishArtifact(with(any(String.class)), with(any(InputStream.class)));
      will(throwException(new IOException("Failure")));

      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));

      oneOf(metadataHandler).validate(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 500);
    m.assertIsSatisfied();
  }

  public void testUploadAlreadyPublishedPackage() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    PackageAnalyzer packageAnalyzer = m.mock(PackageAnalyzer.class);
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);
    Map<String, String> metadata = CollectionsUtil.asMap(
      NuGetPackageAttributes.ID, "Id",
      NuGetPackageAttributes.NORMALIZED_VERSION, "1.0.0");

    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      one(build).getArtifactsLimit();
      will(returnValue(ArtifactsUploadLimit.UNLIMITED));
      oneOf(packageAnalyzer).analyzePackage(with(any(InputStream.class)));
      will(returnValue(metadata));

      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));

      oneOf(metadataHandler).validate(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));
      will(throwException(new PackageExistsException("Package already exists")));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    Assert.assertEquals(response.getStatus(), 409);
    m.assertIsSatisfied();
  }

  public void testOverrideAlreadyPublishedPackage() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    PackageAnalyzer packageAnalyzer = m.mock(PackageAnalyzer.class);
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);
    Map<String, String> metadata = CollectionsUtil.asMap(
      NuGetPackageAttributes.ID, "Id",
      NuGetPackageAttributes.NORMALIZED_VERSION, "1.0.0");

    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      one(build).getArtifactsLimit();
      will(returnValue(ArtifactsUploadLimit.UNLIMITED));
      oneOf(packageAnalyzer).analyzePackage(with(any(InputStream.class)));
      will(returnValue(metadata));
      oneOf(packageAnalyzer).getSha512Hash(with(any(InputStream.class)));
      will(returnValue("hash"));
      oneOf(build).getBuildTypeId();
      will(returnValue("type"));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      one(build).getBuildOwnParameters();
      will(returnValue(Collections.emptyMap()));
      oneOf(build).publishArtifact(with(any(String.class)), with(any(InputStream.class)));
      oneOf(cacheReset).resetCache();

      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));

      oneOf(metadataHandler).validate(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));

      oneOf(metadataHandler).handleMetadata(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));
    }});


    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());
    request.setParameter("replace", "true");

    handler.handleRequest(FEED_DATA, request, response);

    m.assertIsSatisfied();
  }

  public void testUploadValidPackage() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    PackageAnalyzer packageAnalyzer = m.mock(PackageAnalyzer.class);
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);
    Map<String, String> metadata = CollectionsUtil.asMap(
      NuGetPackageAttributes.ID, "Id",
      NuGetPackageAttributes.NORMALIZED_VERSION, "1.0.0");
    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      one(build).getArtifactsLimit();
      will(returnValue(ArtifactsUploadLimit.UNLIMITED));
      oneOf(packageAnalyzer).analyzePackage(with(any(InputStream.class)));
      will(returnValue(metadata));
      oneOf(packageAnalyzer).getSha512Hash(with(any(InputStream.class)));
      will(returnValue("hash"));
      oneOf(build).getBuildTypeId();
      will(returnValue("type"));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      one(build).getBuildOwnParameters();
      will(returnValue(Collections.emptyMap()));
      oneOf(build).publishArtifact(with(any(String.class)), with(any(InputStream.class)));
      oneOf(cacheReset).resetCache();

      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));

      oneOf(metadataHandler).validate(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));

      oneOf(metadataHandler).handleMetadata(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    m.assertIsSatisfied();
  }

  public void testUploadWithoutMaxArtifactSize() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    PackageAnalyzer packageAnalyzer = m.mock(PackageAnalyzer.class);
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);
    Map<String, String> metadata = CollectionsUtil.asMap(
      NuGetPackageAttributes.ID, "Id",
      NuGetPackageAttributes.NORMALIZED_VERSION, "1.0.0");

    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      one(build).getArtifactsLimit();
      will(returnValue(new ArtifactsUploadLimit(-1L, null)));
      oneOf(packageAnalyzer).analyzePackage(with(any(InputStream.class)));
      will(returnValue(metadata));
      oneOf(packageAnalyzer).getSha512Hash(with(any(InputStream.class)));
      will(returnValue("hash"));
      oneOf(build).getBuildTypeId();
      will(returnValue("type"));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      one(build).getBuildOwnParameters();
      will(returnValue(Collections.emptyMap()));
      oneOf(build).publishArtifact(with(any(String.class)), with(any(InputStream.class)));
      oneOf(cacheReset).resetCache();
      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));

      oneOf(metadataHandler).validate(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));

      oneOf(metadataHandler).handleMetadata(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    m.assertIsSatisfied();
  }

  public void testUploadValidPackageAtCustomPath() throws Exception {
    Mockery m = new Mockery();
    RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    RunningBuildEx build = m.mock(RunningBuildEx.class);
    PackageAnalyzer packageAnalyzer = m.mock(PackageAnalyzer.class);
    ResponseCacheReset cacheReset = m.mock(ResponseCacheReset.class);
    ServerSettings serverSettings = m.mock(ServerSettings.class);
    NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerContext> metadataHandler =
      m.mock(NuGetFeedUploadMetadataHandler.class);
    Map<String, String> metadata = CollectionsUtil.asMap(
      NuGetPackageAttributes.ID, "Id",
      NuGetPackageAttributes.NORMALIZED_VERSION, "1.0.0");

    RequestWrapper request = new RequestWrapper(SERVLET_PATH, SERVLET_PATH + "/");
    ResponseWrapper response = new ResponseWrapper(new MockResponse());

    m.checking(new Expectations() {{
      oneOf(runningBuilds).findRunningBuildById(3641L);
      will(returnValue(build));
      one(build).getBuildType();
      will(returnValue(null));
      one(build).getArtifactsLimit();
      will(returnValue(ArtifactsUploadLimit.UNLIMITED));
      oneOf(packageAnalyzer).analyzePackage(with(any(InputStream.class)));
      will(returnValue(metadata));
      oneOf(packageAnalyzer).getSha512Hash(with(any(InputStream.class)));
      will(returnValue("hash"));
      oneOf(build).getBuildTypeId();
      will(returnValue("type"));
      oneOf(build).getAgentAccessCode();
      will(returnValue("code"));
      one(build).getBuildOwnParameters();
      will(returnValue(CollectionsUtil.asMap(NuGetFeedConstants.PROP_NUGET_FEED_PUBLISH_PATH, "{0}.{1}.nupkg")));
      oneOf(build).publishArtifact(with(equal("Id.1.0.0.nupkg")), with(any(InputStream.class)));
      oneOf(cacheReset).resetCache();

      oneOf(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));
      oneOf(serverSettings).getArtifactDirectories();
      File tempDirectory = FileUtil.createTempDirectory("PackageUploadHandlerTests", "test");
      will(returnValue(Arrays.asList(tempDirectory)));

      oneOf(metadataHandler).validate(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));

      oneOf(metadataHandler).handleMetadata(
        with(createMather(request)),
        with(equal(response)),
        with(equal(FEED_DATA)),
        with(equal(build)),
        with(equal("id.1.0.0")),
        with(equal(metadata)));
    }});

    PackageUploadHandler<NuGetFeedUploadHandlerContext> handler =
      new PackageUploadHandler<NuGetFeedUploadHandlerContext>(runningBuilds,packageAnalyzer, cacheReset,
                                                              serverSettings, metadataHandler);

    request.setContentType("multipart/form-data; boundary=\"3576595b-8e57-4d70-91bb-701d5aab54ea\"");
    request.setHeader("X-Nuget-ApiKey", "zxxbe88b7ae8ef92315f0040f7f6522a0f98fae6ee1f6bee6a445f2b24babecd2a3");
    request.setBody(REQUEST_BODY.getBytes());

    handler.handleRequest(FEED_DATA, request, response);

    m.assertIsSatisfied();
  }

  private Matcher<MultipartHttpServletRequest> createMather(HttpServletRequest request) {
    return new BaseMatcher<MultipartHttpServletRequest>() {
      @Override
      public boolean matches(Object o) {
        if (!(o instanceof AbstractMultipartHttpServletRequest)) return false;
        AbstractMultipartHttpServletRequest value = (AbstractMultipartHttpServletRequest)o;
        return request.equals(value.getRequest());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("MultipartHttpServletRequest");
      }
    };
  }
}
