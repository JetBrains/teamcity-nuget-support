/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.FeedCredentials;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.feed.reader.impl.NuGetFeedReaderImpl;
import jetbrains.buildServer.nuget.server.feed.reader.impl.PackagesFeedParserImpl;
import jetbrains.buildServer.nuget.server.feed.reader.impl.UrlResolverImpl;
import jetbrains.buildServer.nuget.tests.integration.http.SimpleThreadedHttpServer;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.SimpleHttpServerBase;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 16:04
 */
public class FeedReaderTest extends BaseTestCase {
  private NuGetFeedReader myReader;
  private FeedHttpClientHolder myClient;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myClient = new FeedHttpClientHolder();
    final FeedGetMethodFactory methods = new FeedGetMethodFactory();
    myReader = new NuGetFeedReaderImpl(new UrlResolverImpl(methods), methods, new PackagesFeedParserImpl());
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    myClient.dispose();
  }

  public static final String DATA_PROVIDER_STATUSES = "FeedReaderLeaks";
  @DataProvider(name = DATA_PROVIDER_STATUSES)
  public Object[][] data_FeedReaderLeaks() {
    return new Object[][] {
            {jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServerBase.STATUS_LINE_200 },
            {jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServerBase.STATUS_LINE_301 },
            {jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServerBase.STATUS_LINE_404 },
            {jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServerBase.STATUS_LINE_500 },
            {jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServerBase.STATUS_LINE_401 },
            {"HTTP/1.0 303 Do not know what is it" },
            {"HTTP/1.0 201 Created" },
            {"HTTP/1.0 503 Something" },
            {"HTTP/1.1 200 Ok" }, //fake 1.1
    };
  }

  @Test(dataProvider = DATA_PROVIDER_STATUSES)
  @TestFor(issues = "TW-25224")
  public void testFeedReaderLeaks(@NotNull final String statusLine) throws IOException, InterruptedException {
    SimpleThreadedHttpServer server = new SimpleThreadedHttpServer(){
      @Override
      protected Response getResponse(String request) {
        String path = "" + getRequestPath(request);

        if (path.startsWith("/feedx")) {
          return createStringResponse(STATUS_LINE_301, Arrays.asList("Location: http://localhost:" + getPort() + "/feedy"), "Moved");
        }

        if (path.startsWith("/feedy")) {
          return createStringResponse(STATUS_LINE_301, Arrays.asList("Location: http://localhost:" + getPort() + "/nuget"), "Moved");
        }

        if (path.startsWith("/feed")) {
          return createStringResponse(STATUS_LINE_301, Arrays.asList("Location: http://localhost:" + getPort() + "/feedx"), "Moved");
        }

        if (path.startsWith("/nuget")) {
          return createStringResponse(statusLine, Collections.<String>emptyList(), "<nop>");
        }

        return createStringResponse(STATUS_LINE_404, Collections.<String>emptyList(), "<nop>");
      }
    };
    server.start();

    final String feedUrl = "http://localhost:" + server.getPort() + "/feed";

    final Runnable toFeedPing = new Runnable() {
      public void run() {
        for(int i = 0; i < 100; i++) {
          try {
            myReader.queryPackageVersions(myClient, feedUrl, "some.foo.bar");
            Thread.sleep(1);
          } catch (Exception e) {
            if (e instanceof org.apache.http.conn.ConnectionPoolTimeoutException) {
              Assert.fail("Pool exhausted");
            }
          }
        }
      }
    };

    try {
      runAsyncAndFailOnException(10, toFeedPing);
    } finally {
      server.stop();
    }
  }

  @Test
  @TestFor(issues = "TW-25224")
  public void testFeedReaderTooManyRedirects() throws IOException, InterruptedException {
    SimpleThreadedHttpServer server = new SimpleThreadedHttpServer(){
      @Override
      protected Response getResponse(String request) {
        String path = "" + getRequestPath(request);

        for(int i = 0; i < 1000; i++) {
          if (path.startsWith("/feed_" + i + "_z")) {
            return createStringResponse(STATUS_LINE_301, Arrays.asList("Location: http://localhost:" + getPort() + "/feed_" + (i+1) + "_z"), "Moved");
          }
        }

        if (path.startsWith("/feed")) {
          return createStringResponse(STATUS_LINE_301, Arrays.asList("Location: http://localhost:" + getPort() + "/feed_0_z"), "Moved");
        }

        if (path.startsWith("/feed_1000_z")) {
          return createStringResponse(STATUS_LINE_301, Arrays.asList("Location: http://localhost:" + getPort() + "/nuget"), "Moved");
        }

        if (path.startsWith("/nuget")) {
          return createStringResponse(STATUS_LINE_200, Collections.<String>emptyList(), "<nop>");
        }

        return createStringResponse(STATUS_LINE_404, Collections.<String>emptyList(), "<nop>");
      }
    };
    server.start();

    final String feedUrl = "http://localhost:" + server.getPort() + "/feed";

    final Runnable toFeedPing = new Runnable() {
      public void run() {
        for(int i = 0; i < 10; i++) {
          try {
            myReader.queryPackageVersions(myClient, feedUrl, "some.foo.bar");
            Thread.sleep(1);
          } catch (Exception e) {
            if (e instanceof org.apache.http.conn.ConnectionPoolTimeoutException) {
              Assert.fail("Pool exhausted");
            }
          }
        }
      }
    };

    try {
      runAsyncAndFailOnException(10, toFeedPing);
    } finally {
      server.stop();
    }
  }

  @Test(enabled = false)
  @TestFor(issues = "TW-21048")
  public void testFollowsNext() throws IOException {
    Collection<FeedPackage> packages = myReader.queryPackageVersions(myClient, FeedConstants.NUGET_FEED_V2, "jonnyzzz.nuget.teamcity.testPackage");
    //NuGet.org feed returns 100 packages per request
    Assert.assertTrue(packages.size() > 100);
  }

  @DataProvider(name = "nuget-feeds")
  public Object[][] nugetFeedsProvider() {
    return new Object[][] {
            {FeedConstants.NUGET_FEED_V1},
            {FeedConstants.NUGET_FEED_V2},
    };
  }

  @Test(dataProvider = "nuget-feeds")
  public void testReadFeed(@NotNull final String feed) throws IOException {
    enableDebug();
    readFeed(feed);
  }

  private void readFeed(String msRefFeed) throws IOException {
    final Collection<FeedPackage> feedPackages = myReader.queryPackageVersions(myClient, msRefFeed, "NuGet.CommandLine");
    Assert.assertTrue(feedPackages.size() > 0);

    boolean hasLatest = false;
    for (FeedPackage feedPackage : feedPackages) {
      Assert.assertFalse(hasLatest && feedPackage.isLatestVersion(), "There could be only one latest");
      hasLatest |= feedPackage.isLatestVersion();
      System.out.println("feedPackage = " + feedPackage);
    }
  }

  @Test(dataProvider = "nuget-feeds")
  public void testDownloadLatest(@NotNull final String feed) throws IOException {
    downloadLatest(feed);
  }

  private void downloadLatest(String feed) throws IOException {
    final Collection<FeedPackage> packages = myReader.queryPackageVersions(myClient, feed, "NuGet.CommandLine");
    FeedPackage latest = null;
    for (FeedPackage aPackage : packages) {
      if (aPackage.isLatestVersion()) {
        latest = aPackage;
      }
    }
    Assert.assertNotNull(latest, "there should be the latest package");

    final File pkd = createTempFile();
    myReader.downloadPackage(myClient, latest.getDownloadUrl(), pkd);

    Assert.assertTrue(pkd.length() > 100);
    boolean hasNuGetExe = false;
    ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(pkd)));
    try {
      for (ZipEntry ze = zip.getNextEntry(); ze != null; ze = zip.getNextEntry()) {
        String name = ze.getName();
        System.out.println("ze = " + name);
        name = name.toLowerCase();
        hasNuGetExe |= name.endsWith("/NuGet.exe".toLowerCase());
        hasNuGetExe |= name.endsWith("\\NuGet.exe".toLowerCase());
      }
    } finally {
      FileUtil.close(zip);
    }

    Assert.assertTrue(hasNuGetExe, "package should contain nuget.exe");
  }


  @Test
  public void test_connection_leaks() throws Exception {
    final SimpleHttpServerBase server = new SimpleHttpServerBase(){
      @Override
      protected Response getResponse(String s) {
        if (s.startsWith("GET /aaa")) {
          return redirectedResponse("bbb");
        }
        if (s.startsWith("GET /bbb")) {
          return redirectedResponse("ccc");
        }
        if (s.startsWith("GET /ccc")) {
          return redirectedResponse("ddd");
        }
        if (s.startsWith("GET /ddd")) {
          return redirectedResponse("qqq");
        }
        if (s.startsWith("GET /qqq")) {
          return new Response("HTTP/1.0 200 Ok", Arrays.asList("Encoding: utf-8", "Content-Type: text/xml")) {
            @Override
            public void printContent(PrintStream printStream) throws IOException {
              final File path = Paths.getTestDataPath("feed/reader/feed-response.xml");
              InputStream fis = new BufferedInputStream(new FileInputStream(path));
              FileUtil.copyStreams(fis, printStream);
            }

            @Override
            public Integer getLength() {
              return null;
            }
          };
        }
        return null;
      }

      private Response redirectedResponse(String next) {
        final String url = "http://localhost:" + getPort() + "/" + next;
        return new Response("HTTP/1.0 301 Moved", Arrays.asList("Location: " + url)) {
          @Override
          public void printContent(PrintStream printStream) throws IOException {
          }

          @Override
          public Integer getLength() {
            return null;
          }
        };
      }
    };

    server.start();
    try {
      for(int i = 0; i <100; i++) {
        myReader.queryPackageVersions(myClient, "http://localhost:" + server.getPort() + "/aaa", "NuGet");
      }
    } finally {
      server.stop();
    }
  }

  @Test
  @TestFor(issues = "TW-23193")
  public void test_proxy_reply() throws Exception {
    final SimpleHttpServerBase server = new SimpleHttpServerBase(){
      @Override
      protected Response getResponse(String s) {
        if (s.startsWith("GET /aaa")) {
          return createStringResponse(STATUS_LINE_200, Arrays.asList("Encoding: utf-8", "Content-Type: text/html"), "this is not a nuget server. It looks your corporate proxt banned our lively NuGet feed. Don't let Xml parser > to parse < < <  this");
        }
        return null;
      }
    };

    try {
      server.start();
      try {
        myReader.queryPackageVersions(myClient, "http://localhost:" + server.getPort() + "/aaa", "NuGet");
        Assert.fail();
      } catch (IOException e) {
        Assert.assertTrue(e.getMessage().contains("Failed to parse output from NuGet feed. Check feed url:"));
      }
    } finally {
      server.stop();
    }
  }

  @Test
  @TestFor(issues = "TW-20764")
  public void test_auth_supported() throws Throwable {
    MockNuGetAuthHTTP.executeTest(new MockNuGetAuthHTTP.Action() {
      public void runTest(@NotNull MockNuGetAuthHTTP http) throws Throwable {
        FeedClient cli = myClient.withCredentials(http.getCredentials());
        Collection<FeedPackage> result = myReader.queryPackageVersions(cli, http.getSourceUrl(), "FineCollection");
        Assert.assertFalse(result.isEmpty());
      }
    });
  }

  @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = ".*Wrong username or password.*")
  @TestFor(issues = "TW-20764")
  public void test_auth_supported_wrong_creds() throws Throwable {
    MockNuGetAuthHTTP.executeTest(new MockNuGetAuthHTTP.Action() {
      public void runTest(@NotNull MockNuGetAuthHTTP http) throws Throwable {
        FeedClient cli = myClient.withCredentials(new FeedCredentials("aaa", "qqq"));
        Collection<FeedPackage> result = myReader.queryPackageVersions(cli, http.getSourceUrl(), "FineCollection");
        Assert.assertFalse(result.isEmpty());
      }
    });
  }

}
