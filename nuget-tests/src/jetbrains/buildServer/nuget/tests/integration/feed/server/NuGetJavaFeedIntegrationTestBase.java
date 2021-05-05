/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import com.google.gson.annotations.JsonAdapter;
import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.controllers.MockResponse;
import jetbrains.buildServer.nuget.common.index.PackageAnalyzer;
import jetbrains.buildServer.nuget.common.index.PackageConstants;
import jetbrains.buildServer.nuget.common.version.VersionUtility;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.NuGetUtils;
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCache;
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCacheReset;
import jetbrains.buildServer.nuget.feed.server.controllers.*;
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedStdUploadHandler;
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadHandlerStdContext;
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadMetadataHandler;
import jetbrains.buildServer.nuget.feed.server.controllers.upload.PackageUploadHandler;
import jetbrains.buildServer.nuget.feed.server.index.*;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackagesIndexImpl;
import jetbrains.buildServer.nuget.feed.server.index.impl.SemanticVersionsComparators;
import jetbrains.buildServer.nuget.feed.server.index.impl.transform.DownloadUrlComputationTransformation;
import jetbrains.buildServer.nuget.feed.server.json.*;
import jetbrains.buildServer.nuget.feed.server.odata4j.ODataRequestHandler;
import jetbrains.buildServer.nuget.feed.server.olingo.OlingoRequestHandler;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.serverSide.RunningBuildsCollection;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.util.PathModifiers;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.jmock.lib.legacy.ClassImposteriser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import static jetbrains.buildServer.nuget.common.index.PackageConstants.NUGET_PROVIDER_ID;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 04.01.12 23:55
 */
public class NuGetJavaFeedIntegrationTestBase extends NuGetFeedIntegrationTestBase {
  protected static final NuGetFeedData FEED_DATA = NuGetFeedData.DEFAULT;
  protected static final String SERVLET_PATH = "/app/nuget/feed/_Root/default/v2";
  protected static final String SERVLET_V3_PATH = "/app/nuget/feed/_Root/default/v3";
  protected static final String DOWNLOAD_URL = "/downlaodREpoCon/downlaod-url";
  protected PackagesIndex myIndex;
  protected PackagesIndex myActualIndex;
  protected PackagesIndex myIndexProxy;
  protected MetadataStorage myMetadataStorage;
  private SortedList<NuGetIndexEntry> myFeed;
  private NuGetServerSettings mySettings;
  protected NuGetFeedProvider myFeedProvider;
  protected NuGetFeedFactory myFeedFactory;
  private int myCount;
  protected String myContextPath;
  protected String myAuthenticationType;
  protected NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerStdContext> myMetadataHandler;
  private JsonPackageSourceFactory myPackageSourceFactory;
  private JsonPackageAdapterFactory myAdapterFactory;

  @Parameters({ "contextPath", "authenticationType" })
  @BeforeMethod
  protected void setUp(
    @Optional("") final String contextPath,
    @Optional("") final String authenticationType
  ) throws Exception {
    super.setUp();
    final Mockery mockery = new Mockery() {{
      setImposteriser(ClassImposteriser.INSTANCE);
    }};
    myCount = 0;
    myFeed = new SortedList<>(SemanticVersionsComparators.getEntriesComparator());
    myIndex = m.mock(PackagesIndex.class);
    myActualIndex = myIndex;
    myIndexProxy = m.mock(PackagesIndex.class, "proxy");
    mySettings = m.mock(NuGetServerSettings.class);
    myMetadataStorage = m.mock(MetadataStorage.class);
    myFeedFactory = m.mock(NuGetFeedFactory.class);
    myContextPath = contextPath;
    myAuthenticationType = authenticationType;
    myMetadataHandler = m.mock(NuGetFeedUploadMetadataHandler.class);
    myPackageSourceFactory = m.mock(JsonPackageSourceFactory.class);
    myAdapterFactory = new JsonPackageAdapterFactoryImpl();

    final ResponseCache responseCache = m.mock(ResponseCache.class);
    final RunningBuildsCollection runningBuilds = m.mock(RunningBuildsCollection.class);
    final PackageAnalyzer packageAnalyzer = mockery.mock(PackageAnalyzer.class);
    final ResponseCacheReset cacheReset = mockery.mock(ResponseCacheReset.class);
    final ServerSettings serverSettings = mockery.mock(ServerSettings.class);

    m.checking(new Expectations() {{
      allowing(myIndexProxy).getAll();
      will(new CustomAction("lazy return packages") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myFeed;
        }
      });
      allowing(myIndexProxy).search(with(any(Collection.class)), with(equal("noSuchPackage")));
      will(returnValue(Collections.emptyList()));
      allowing(myIndexProxy).search(with(any(Collection.class)), with(any(String.class)));
      will(returnValue(myFeed));
      allowing(myIndexProxy).find(with(equal(CollectionsUtil.asMap("Id", "skiptoken"))));
      will(new CustomAction("lazy return packages") {
        public Object invoke(Invocation invocation) throws Throwable {
          return CollectionsUtil.filterCollection(myFeed, entry -> entry.getKey().startsWith("skiptoken"));
        }
      });
      allowing(myIndexProxy).find(with(any(Map.class)));
      will(new CustomAction("lazy return packages") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myFeed;
        }
      });
      allowing(myIndexProxy).getByKey(with(equal("skiptoken.1.0.102")));
      will(new CustomAction("lazy return packages") {
        public Object invoke(Invocation invocation) throws Throwable {
          return Collections.singletonList(CollectionsUtil.findFirst(myFeed, item -> item.getKey().equals("skiptoken.1.0.102")));
        }
      });
      allowing(myIndexProxy).getByKey(with(any(String.class)));
      will(returnValue(myFeed));
      allowing(myIndex).getAll();
      will(returnValue(myFeed));
      allowing(mySettings).isFilteringByTargetFrameworkEnabled();
      will(returnValue(true));

      allowing(myMetadataStorage).getAllEntries(NUGET_PROVIDER_ID);
      will(new CustomAction("transform entries") {
        public Object invoke(Invocation invocation) throws Throwable {
          return toEntries(myFeed).iterator();
        }
      });

      allowing(serverSettings).getRootUrl();
      will(returnValue("http://localhost:8111"));

      final NuGetFeed feed = new NuGetFeed(myIndexProxy, mySettings);
      allowing(myFeedFactory).createFeed(with(any(NuGetFeedData.class)));
      will(returnValue(feed));

      final JsonPackageSource source = new JsonPackageSourceImpl(feed);
      allowing(myPackageSourceFactory).create(with(any(NuGetFeed.class)));
      will(returnValue(source));
    }});

    final ODataRequestHandler oDataRequestHandler = new ODataRequestHandler(myFeedFactory, responseCache);
    final OlingoRequestHandler olingoRequestHandler = new OlingoRequestHandler(myFeedFactory, responseCache);
    final NuGetFeedStdUploadHandler uploadHandler =
      new NuGetFeedStdUploadHandler(new PackageUploadHandler<NuGetFeedUploadHandlerStdContext>(runningBuilds, packageAnalyzer, cacheReset, serverSettings, myMetadataHandler));
    final JsonRequestHandler jsonRequestHandler = new JsonRequestHandler(
      new JsonServiceIndexHandler(),
      new JsonSearchQueryHandler(myFeedFactory, myPackageSourceFactory, myAdapterFactory),
      new JsonRegistrationHandler(myFeedFactory, myPackageSourceFactory, myAdapterFactory),
      new JsonPackageContentHandler(myFeedFactory, myPackageSourceFactory, myAdapterFactory),
      new JsonAutocompleteHandler(myFeedFactory)
    );
    myFeedProvider = new NuGetFeedProviderImpl(oDataRequestHandler, olingoRequestHandler, jsonRequestHandler, uploadHandler);

    final PathModifiers pathModifiers = new PathModifiers();
    pathModifiers.init();
    pathModifiers.registerPathModifier(new PathModifiers.PathModifier() {
      @Override
      public boolean matches(@NotNull final String path) {
        if (StringUtil.isEmptyOrSpaces(myAuthenticationType) || StringUtil.isEmptyOrSpaces(path)) {
          return false;
        }
        return path.startsWith(myAuthenticationType + "/");
      }

      @Override
      public String modifyPath(@NotNull final String path) {
        if (StringUtil.isEmptyOrSpaces(path)) {
          return path;
        }
        return path.substring(myAuthenticationType.length());
      }
    });
  }

  @NotNull
  private static Collection<BuildMetadataEntry> toEntries(Iterable<NuGetIndexEntry> feed) {
    Collection<BuildMetadataEntry> ee = new ArrayList<>();
    for (final NuGetIndexEntry e : feed) {
      ee.add(new BuildMetadataEntry() {
        public long getBuildId() {
          return e.hashCode();
        }

        @NotNull
        public String getKey() {
          return e.getKey();
        }

        @NotNull
        public Map<String, String> getMetadata() {
          return e.getAttributes();
        }
      });
    }
    return ee;
  }

  private void setPackagesIndex(@NotNull PackagesIndex index) {
    myActualIndex = index;
  }

  protected void enablePackagesIndexSorting() {
    setPackagesIndex(new PackagesIndexImpl(
            FEED_DATA,
            myMetadataStorage,
            Arrays.asList(
                    new MockExternalIdTransformation(),
                    new DownloadUrlComputationTransformation()
            )
    ));
  }

  protected NuGetAPIVersion getAPIVersion() {
    return NuGetAPIVersion.V2;
  }

  @Override
  protected String getNuGetServerUrl() {
    return getServerUrl() + myAuthenticationType + NuGetUtils.getProjectFeedPath(FEED_DATA.getProjectId(), FEED_DATA.getFeedId(), getAPIVersion()) + "/";
  }

  protected String getServerUrl() { return "http://localhost" + myContextPath; }

  protected String getServletPath() {
    return myAuthenticationType + NuGetUtils.getProjectFeedPath(FEED_DATA.getProjectId(), FEED_DATA.getFeedId()) + getAPIVersion().name().toLowerCase();
  }

  protected NuGetIndexEntry addPackage(@NotNull final File file, boolean isLatest) throws IOException {
    final Map<String, String> map = indexPackage(file, isLatest);
    NuGetIndexEntry e = new NuGetIndexEntry(FEED_DATA, file.getName(), map);
    myFeed.add(e);
    return e;
  }

  @NotNull
  private Map<String, String> indexPackage(@NotNull final File file, final boolean isLatest) throws IOException {
    final int buildId = myCount++;
    final Map<String, String> map = indexPackage(file, isLatest, buildId);
    map.put(PackageConstants.TEAMCITY_DOWNLOAD_URL, DOWNLOAD_URL);
    return map;
  }

  protected NuGetIndexEntry addMockPackage(@NotNull final NuGetIndexEntry entry) {
    return addMockPackage(entry, false);
  }

  protected NuGetIndexEntry addMockPackage(@NotNull final String key, @NotNull final Map<String, String> params) {
    return addMockPackage(key, params, false);
  }

  protected NuGetIndexEntry addMockPackage(@NotNull final String key, @NotNull final Map<String, String> params, boolean isLatest) {
    return addMockPackage(new NuGetIndexEntry(FEED_DATA, key, params), isLatest);
  }

  protected NuGetIndexEntry addMockPackage(@NotNull final NuGetIndexEntry entry, boolean isLatest) {
    final Map<String, String> map = new HashMap<>(entry.getAttributes());

    final String id = entry.getAttributes().get(ID);
    final String ver = entry.getAttributes().get(VERSION);

    map.put(VERSION, ver + "." + myCount);
    map.put(IS_LATEST_VERSION, String.valueOf(isLatest));
    map.put(IS_ABSOLUTE_LATEST_VERSION, String.valueOf(isLatest));
    map.put(PackageConstants.TEAMCITY_DOWNLOAD_URL, DOWNLOAD_URL);
    NuGetIndexEntry e = new NuGetIndexEntry(FEED_DATA, id + "." + ver, map);
    myFeed.add(e);
    return e;
  }

  protected NuGetIndexEntry addMockPackage(@NotNull final Map<String, String> attributes) {
    attributes.put(PackageConstants.TEAMCITY_DOWNLOAD_URL, DOWNLOAD_URL);

    final String id = attributes.get(ID);
    final String ver = attributes.get(VERSION);
    NuGetIndexEntry e = new NuGetIndexEntry(FEED_DATA, id + "." + ver, attributes);

    myFeed.add(e);
    return e;
  }

  @NotNull
  protected NuGetIndexEntry addMockPackage(@NotNull final String id, @NotNull final String ver) throws IOException {
    final Map<String, String> map = new TreeMap<>(indexPackage(Paths.getTestDataPath("packages/NuGet.Core.1.5.20902.9026.nupkg"), true));

    map.put(ID, id);
    map.put(VERSION, ver);
    map.put(NORMALIZED_VERSION, VersionUtility.normalizeVersion(ver));

    map.remove(IS_LATEST_VERSION);
    map.remove(IS_ABSOLUTE_LATEST_VERSION);
    map.put(PackageConstants.TEAMCITY_DOWNLOAD_URL, DOWNLOAD_URL);
    NuGetIndexEntry e = new NuGetIndexEntry(FEED_DATA, id + "." + ver, map);
    myFeed.add(e);
    return e;
  }

  protected void dumpFeed() {
    for (NuGetIndexEntry e : myIndexProxy.getAll()) {
      final Map<String, String> a = e.getAttributes();
      System.out.println(a.get(ID) + " " + a.get(VERSION) + " => absolute:" + a.get(IS_ABSOLUTE_LATEST_VERSION) + ", latest: " + a.get(IS_LATEST_VERSION) + ", prerelease: " + a.get(IS_PRERELEASE));
    }
  }

  protected void assertContainsPackageVersion(String responseBody, String version) {
    assertContains(responseBody, "<d:Version>" + version + "</d:Version>");
  }

  protected void assertNotContainsPackageVersion(String responseBody, String version) {
    assertNotContains(responseBody, "<d:Version>" + version + "</d:Version>", false);
  }

  protected void assertPackageVersionsOrder(String responseBody, String... versions) {
    int prevVersionPosition = 0;
    for (String version : versions) {
      final int i = responseBody.indexOf("<d:Version>" + version + "</d:Version>");
      if (i == -1) fail("Response doesn't contain package version " + version);
      assertGreater(i, prevVersionPosition);
      prevVersionPosition = i;
    }
  }

  protected void setODataSerializer(final NugetFeedLibrary library) {
    boolean useOlingo = library.equals(NugetFeedLibrary.Olingo);
    setInternalProperty(NuGetFeedConstants.PROP_NUGET_FEED_NEW_SERIALIZER, Boolean.toString(useOlingo));
  }

  @Override
  @NotNull
  protected String openRequest(@NotNull final String requestUrl, @NotNull final NameValuePair... reqs) {
    return processRequest(createRequest(requestUrl, reqs)).toString();
  }

  @NotNull
  protected HttpServletRequest createRequest(@NotNull final String requestUrl, @NotNull final NameValuePair... reqs) {
    final String servletPath = getServletPath();
    final RequestWrapper requestWrapper = new RequestWrapper(myContextPath, servletPath, myContextPath + servletPath + "/" + requestUrl);
    for (NameValuePair req : reqs) {
      requestWrapper.setParameter(req.getName(), req.getValue());
    }
    return requestWrapper;
  }

  @Override
  @Nullable
  protected <T> T execute(@NotNull final HttpRequestBase get, @NotNull final ExecuteAction<T> action) {
    final URI uri = get.getURI();
    final String path = uri.getRawPath() + (StringUtil.isEmpty(uri.getRawQuery()) ? StringUtil.EMPTY : "?" + uri.getRawQuery());
    final RequestWrapper request = new RequestWrapper(myContextPath, getServletPath(), path);

    final ResponseWrapper response = processRequest(request);

    final BasicHttpResponse httpResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), response.getStatus(), "");
    try {
      httpResponse.setEntity(new StringEntity(response.toString()));
      return action.processResult(httpResponse);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  protected ResponseWrapper processRequest(@NotNull final HttpServletRequest request) {
    request.setAttribute(NuGetFeedConstants.NUGET_FEED_API_VERSION, getAPIVersion());
    final NuGetFeedHandler handler = myFeedProvider.getHandler(request);
    final MockResponse response = new MockResponse();
    final ResponseWrapper responseWrapper = new ResponseWrapper(response);

    try {
      handler.handleRequest(FEED_DATA, request, responseWrapper);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return responseWrapper;
  }

  protected enum NugetFeedLibrary {
    OData4j, Olingo
  }

  @DataProvider
  protected Object[][] nugetFeedLibrariesData() {
    return new Object[][]{
            {NugetFeedLibrary.OData4j},
            {NugetFeedLibrary.Olingo}
    };
  }
}
