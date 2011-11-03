/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.server.exec.impl.NuGetExecutorImpl;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.nuget.server.feed.reader.impl.NuGetFeedReaderImpl;
import jetbrains.buildServer.nuget.server.feed.reader.impl.PackagesFeedParserImpl;
import jetbrains.buildServer.nuget.server.feed.reader.impl.UrlResolverImpl;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerTokens;
import jetbrains.buildServer.nuget.server.feed.server.controllers.MetadataControllersPaths;
import jetbrains.buildServer.nuget.server.feed.server.controllers.MetadataControllersPathsImpl;
import jetbrains.buildServer.nuget.server.feed.server.controllers.PackageInfoSerializer;
import jetbrains.buildServer.nuget.server.feed.server.impl.NuGetServerTokensImpl;
import jetbrains.buildServer.nuget.server.feed.server.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.nuget.server.feed.server.process.NuGetServerRunner;
import jetbrains.buildServer.nuget.server.feed.server.process.NuGetServerRunnerImpl;
import jetbrains.buildServer.nuget.server.feed.server.process.NuGetServerUriImpl;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
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

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static jetbrains.buildServer.nuget.server.feed.server.PackagesIndex.TEAMCITY_ARTIFACT_RELPATH;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 24.10.11 18:09
 */
public class NuGetServerIntegrationTestBase extends BaseTestCase {
  protected Mockery m;
  protected Collection<InputStream> myStreams;
  private NuGetTeamCityProvider myProvider;
  private File myLogsFile;

  private FeedHttpClientHolder nyHttpClient;
  private FeedGetMethodFactory myHttpMethods;
  protected NuGetFeedReaderImpl myFeedReader;
  protected NuGetServerRunnerTokens myTokens;
  protected MetadataControllersPaths myPaths;

  private SimpleHttpServer myHttpServer;
  private NuGetServerRunner myNuGetServer;
  private String myHttpServerUrl;
  protected  NuGetServerUriImpl myNuGetServerAddresses;
  protected String myNuGetServerUrl;


  private Collection<HttpServerHandler> myHandlers;

  protected String myHttpContextUrl;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myStreams = new ArrayList<InputStream>();
    myProvider = m.mock(NuGetTeamCityProvider.class);
    myLogsFile = createTempFile();

    nyHttpClient = new FeedHttpClientHolder();
    myHttpMethods = new FeedGetMethodFactory();
    myFeedReader = new NuGetFeedReaderImpl(nyHttpClient, new UrlResolverImpl(nyHttpClient, myHttpMethods), myHttpMethods, new PackagesFeedParserImpl());
    myTokens = new NuGetServerTokensImpl();

    myHttpServer = null;
    myNuGetServer = null;
    myHttpServerUrl = null;
    myNuGetServerAddresses = null;
    myNuGetServerUrl = null;

    final PluginDescriptor descriptor = m.mock(PluginDescriptor.class);
    myPaths = new MetadataControllersPathsImpl(descriptor, myTokens);

    m.checking(new Expectations() {{
      allowing(myProvider).getNuGetServerRunnerPath();
      will(returnValue(Paths.getNuGetServerRunnerPath()));

      allowing(descriptor).getPluginResourcesPath(with(any(String.class)));
      will(new CustomAction("map path") {
        public Object invoke(Invocation invocation) throws Throwable {
          return "/nuget/" + invocation.getParameter(0);
        }
      });
    }});

    myHandlers = new CopyOnWriteArrayList<HttpServerHandler>();

    myHttpContextUrl = "/" + jetbrains.buildServer.util.StringUtil.generateUniqueHash();

    startSimpleHttpServer();
    startNuGetServer();
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    for (InputStream stream : myStreams) {
      FileUtil.close(stream);
    }

    System.out.println("NuGet Feed server logs: \r\n" + loadAllText(myLogsFile));

    if (myHttpServer != null) {
      myHttpServer.stop();
    }

    if (myNuGetServer != null) {
      myNuGetServer.stopServer();
    }

    super.tearDown();
  }

  private void startNuGetServer() {
    final NuGetServerRunnerSettings settings = m.mock(NuGetServerRunnerSettings.class);
    m.checking(new Expectations() {{
      allowing(settings).getPackagesControllerUrl(); will(returnValue(myHttpServerUrl));
      allowing(settings).getLogFilePath(); will(returnValue(myLogsFile));
    }});

    myNuGetServer = new NuGetServerRunnerImpl(settings, myTokens, new NuGetExecutorImpl(myProvider));
    myNuGetServer.startServer();

    myNuGetServerAddresses = new NuGetServerUriImpl(myNuGetServer);
    myNuGetServerUrl = myNuGetServerAddresses.getNuGetFeedBaseUri();
    System.out.println("Created nuget server at: " + myNuGetServerUrl);
  }

  private void startSimpleHttpServer() throws IOException {
    myHttpServer = new SimpleHttpServer() {
      @Override
      protected Response getResponse(String request) {
        System.out.println("Mock HTTP server request: " + request);

        for (HttpServerHandler handler : myHandlers) {
          Response r = handler.processRequest(request, getRequestPath(request));
          if (r != null) return r;
        }

        return super.getResponse(request);
      }
    };
    myHttpServer.start();
    myHttpServerUrl = "http://localhost:" + myHttpServer.getPort() + myHttpContextUrl;
    System.out.println("Created http server at: " + myHttpServerUrl);
  }


  protected void registerHttpHandler(@NotNull final HttpServerHandler handler) {
    myHandlers.add(handler);
  }

  protected boolean checkContainsToken(@NotNull final String request) {
    return request.contains(myTokens.getAccessToken()) && request.contains(myTokens.getAccessTokenHeaderName());
  }


  protected String loadAllText(File temp) throws IOException {
    return new String(FileUtil.loadFileText(temp, "utf-8"));
  }

  protected Runnable assertOwn() {
    return new Runnable() {
      public void run() {
        final HttpGet get = myHttpMethods.createGet(myHttpServerUrl);
        try {
          final HttpResponse execute = nyHttpClient.execute(get);
          final HttpEntity entity = execute.getEntity();
          System.out.println("Own server Request: " + get.getRequestLine());
          entity.writeTo(System.out);
          System.out.println();
          System.out.println();

          Assert.assertTrue(execute.getStatusLine().getStatusCode() == SC_OK);
        } catch (IOException e) {
          throw new RuntimeException("Failed to connect to " + get.getRequestLine() + ". " + e.getClass() + " " + e.getMessage(), e);
        } finally {
          get.abort();
        }
      }
    };
  }

  protected Runnable assert200(@NotNull final String req,
                               @NotNull final NameValuePair... reqs) {
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

  protected void renderPackagesResponseFile(@NotNull final File responseFile,
                                            @NotNull final File... packagesFile) throws PackageLoadException, IOException {
    final Writer w = new OutputStreamWriter(new FileOutputStream(responseFile), "utf-8");
    w.append("                 ");

    for (final File packageFile : packagesFile) {
      final SFinishedBuild build = m.mock(SFinishedBuild.class, "build-" + packageFile.getPath());
      final BuildArtifact artifact = m.mock(BuildArtifact.class, "artifact-" + packageFile.getPath());

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
      final Map<String, String> map = new HashMap<String, String>(factory.loadPackage(artifact));
      map.put(TEAMCITY_ARTIFACT_RELPATH, "some/package/download/" + packageFile.getName());

      new PackageInfoSerializer(myPaths).serializePackage(map, build, true, w);
      w.append("                 ");
    }

    FileUtil.close(w);
    System.out.println("Generated response file: " + responseFile);

    String text = loadAllText(responseFile);
    System.out.println("Generated server response:\r\n" + text);
  }
}
