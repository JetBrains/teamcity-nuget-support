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

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolDownloader;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.DownloadableNuGetTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.NuGetToolDownloaderImpl;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetToolDownloaderTest extends BaseTestCase {
  private NuGetToolDownloader myDownloader;
  private FeedClient myClient;
  private NuGetFeedReader myFeed;
  private Mockery m;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myClient = m.mock(FeedClient.class);
    myFeed = m.mock(NuGetFeedReader.class);
    myDownloader = new NuGetToolDownloaderImpl(myFeed, myClient);
  }

  @Test(expectedExceptions = ToolException.class)
  public void testFeedFile_downloadFail() throws ToolException, IOException {
    m.checking(new Expectations(){{

      oneOf(myFeed).downloadPackage(with(equal(myClient)), with(equal("download-url")), with(any(File.class))); will(throwException(new IOException("oops")));
    }});

    myDownloader.downloadTool(tool("id", "version", "download-url"));
  }

  private DownloadableNuGetTool tool(final String id, final String version, final String downloadUrl) {
    return new DownloadableNuGetTool() {
      @NotNull
      public String getDownloadUrl() {
        return downloadUrl;
      }

      @NotNull
      public String getId() {
        return id;
      }

      @NotNull
      public String getVersion() {
        return version;
      }
    };
  }
}
