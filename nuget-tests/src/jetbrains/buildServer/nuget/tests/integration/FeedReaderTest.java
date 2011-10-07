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

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.feed.reader.impl.*;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.SimpleHttpServerBase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
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
    myReader = new NuGetFeedReaderImpl(myClient, new UrlResolverImpl(myClient, methods), methods, new PackagesFeedParserImpl());
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    myClient.dispose();
  }

  @Test
  public void testRead_MS() throws IOException {
    readFeed(FeedConstants.MS_REF_FEED);
  }

  @Test
  public void testRead_NUGET() throws IOException {
    readFeed(FeedConstants.NUGET_FEED);
  }

  private void readFeed(String msRefFeed) throws IOException {
    final Collection<FeedPackage> feedPackages = myReader.queryPackageVersions(msRefFeed, "NuGet.CommandLine");
    Assert.assertTrue(feedPackages.size() > 0);

    boolean hasLatest = false;
    for (FeedPackage feedPackage : feedPackages) {
      Assert.assertFalse(hasLatest && feedPackage.isLatestVersion(), "There could be only one latest");
      hasLatest |= feedPackage.isLatestVersion();
      System.out.println("feedPackage = " + feedPackage);
    }
  }

  @Test
  public void testDownloadLatest_MS() throws IOException {
    downloadLatest(FeedConstants.MS_REF_FEED);
  }

  @Test
  public void testDownloadLatest_NUGET() throws IOException {
    downloadLatest(FeedConstants.NUGET_FEED);
  }

  private void downloadLatest(String feed) throws IOException {
    final Collection<FeedPackage> packages = myReader.queryPackageVersions(feed, "NuGet.CommandLine");
    FeedPackage latest = null;
    for (FeedPackage aPackage : packages) {
      if (aPackage.isLatestVersion()) {
        latest = aPackage;
      }
    }
    Assert.assertNotNull(latest, "there should be the latest package");

    final File pkd = createTempFile();
    myReader.downloadPackage(latest, pkd);

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
        myReader.queryPackageVersions("http://localhost:" + server.getPort() + "/aaa", "NuGet");
      }
    } finally {
      server.stop();
    }
  }
}
