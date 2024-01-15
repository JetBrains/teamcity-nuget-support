

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import com.google.common.collect.Lists;
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedGetMethodFactory;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedHttpClientHolder;
import jetbrains.buildServer.nuget.common.index.FrameworkConstraintsCalculator;
import jetbrains.buildServer.nuget.common.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.common.index.NuGetPackageStructureAnalyser;
import jetbrains.buildServer.nuget.common.index.NuGetPackageStructureVisitor;
import jetbrains.buildServer.nuget.common.version.FrameworkConstraints;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.*;
import java.util.*;

import static jetbrains.buildServer.nuget.common.index.PackageConstants.*;
import static jetbrains.buildServer.nuget.feed.server.index.PackagesIndex.*;
import static org.apache.http.HttpStatus.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.01.12 0:57
 */
public abstract class NuGetFeedIntegrationTestBase extends IntegrationTestBase {
  private Collection<InputStream> myStreams;
  private NuGetFeedHttpClientHolder myHttpClient;
  private NuGetFeedGetMethodFactory myHttpMethods;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myStreams = new ArrayList<>();
    myHttpClient = new NuGetFeedHttpClientHolder();
    myHttpMethods = new NuGetFeedGetMethodFactory();

  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    myStreams.forEach(FileUtil::close);
  }

  protected abstract String getNuGetServerUrl();

  @NotNull
  protected HttpGet createGetQuery(@NotNull String req, @NotNull final NameValuePair... reqs) {
    return myHttpMethods.createGet(getNuGetServerUrl() + req, reqs);
  }

  protected interface ExecuteAction<T> {
    T processResult(@NotNull HttpResponse response) throws IOException;
  }

  protected <T> T execute(@NotNull final HttpRequestBase get, @NotNull final ExecuteAction<T> action) {
    try {
      final HttpResponse execute = myHttpClient.execute(get);
      return action.processResult(execute);
    } catch (IOException e) {
      throw new RuntimeException("Failed to connect to " + get.getRequestLine() + ". " + e.getClass() + " " + e.getMessage(), e);
    } finally {
      get.abort();
    }
  }


  @NotNull
  protected Map<String, String> indexPackage(@NotNull final File packageFile,
                                             final boolean isLatest,
                                             final long buildId) throws IOException {
    final SFinishedBuild build = m.mock(SFinishedBuild.class, "build-" + packageFile.getPath() + "#" + buildId);
    final BuildArtifact artifact = m.mock(BuildArtifact.class, "artifact-" + packageFile.getPath() + "#" + buildId);

    m.checking(new Expectations() {{
      allowing(build).getBuildId();
      will(returnValue(buildId));
      allowing(build).getBuildTypeId();
      will(returnValue("bt"));
      allowing(build).getBuildTypeName();
      will(returnValue("buidldzzz"));
      allowing(build).getFinishDate();
      will(returnValue(new Date(1319214849319L)));

      allowing(artifact).getTimestamp();
      will(returnValue(packageFile.lastModified()));
      allowing(artifact).getSize();
      will(returnValue(packageFile.length()));
      allowing(artifact).getRelativePath();
      will(returnValue(packageFile.getPath()));
      allowing(artifact).getName();
      will(returnValue(packageFile.getName()));
    }});

    final LocalNuGetPackageItemsFactory packageItemsFactory = new LocalNuGetPackageItemsFactory();
    final FrameworkConstraintsCalculator frameworkConstraintsCalculator = new FrameworkConstraintsCalculator();
    final List<NuGetPackageStructureAnalyser> analysers = Lists.newArrayList(frameworkConstraintsCalculator, packageItemsFactory);

    new NuGetPackageStructureVisitor(analysers).visit(new FileInputStream(packageFile));

    final Map<String, String> map = packageItemsFactory.getItems();
    map.put(TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(frameworkConstraintsCalculator.getPackageConstraints()));
    map.put(TEAMCITY_ARTIFACT_RELPATH, "some/package/download/" + packageFile.getName());
    map.put(TEAMCITY_BUILD_TYPE_ID, "bt_" + packageFile.getName());
    map.put("TeamCityDownloadUrl", "some-download-url/" + packageFile.getName());
    map.put(NuGetPackageAttributes.IS_LATEST_VERSION, String.valueOf(isLatest));
    return map;
  }

  @NotNull
  protected String openRequest(@NotNull final String req,
                               @NotNull final NameValuePair... reqs) throws Exception {
    final HttpGet get = createGetQuery(req, reqs);
    return execute(get, response -> {
      final HttpEntity entity = response.getEntity();
      System.out.println("Request: " + get.getRequestLine());
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      entity.writeTo(bos);
      String enc = null;
      final Header encE = entity.getContentEncoding();
      if (encE != null) {
        enc = encE.getValue();
      }
      if (StringUtil.isEmptyOrSpaces(enc)) {
        enc = "utf-8";
      }

      return bos.toString(enc);
    });
  }

  @NotNull
  protected Runnable assert200(@NotNull final String req,
                               @NotNull final NameValuePair... reqs) {
    return assertStatusCode(SC_OK, req, reqs);
  }

  @NotNull
  protected Runnable assert400(@NotNull final String req,
                               @NotNull final NameValuePair... reqs) {
    return assertStatusCode(SC_BAD_REQUEST, req, reqs);
  }

  @NotNull
  protected Runnable assert404(@NotNull final String req,
                               @NotNull final NameValuePair... reqs) {
    return assertStatusCode(SC_NOT_FOUND, req, reqs);
  }

  @NotNull
  protected Runnable assertStatusCode(final int statusCode,
                                    @NotNull final String req,
                                    @NotNull final NameValuePair... reqs) {
    return () -> {
      final HttpGet get = createGetQuery(req, reqs);
      execute(get, response -> {
        System.out.println("Request: " + get.getRequestLine());

        final HttpEntity entity = response.getEntity();
        if (entity != null) {
          entity.writeTo(System.out);
          System.out.println();
        }

        Assert.assertEquals(response.getStatusLine().getStatusCode(), statusCode);
        return null;
      });
    };
  }
}
