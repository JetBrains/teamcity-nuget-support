/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.*;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.NuGetToolDownloaderImpl;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.NuGetToolsInstallerImpl;
import jetbrains.buildServer.nuget.tests.Strings;
import jetbrains.buildServer.util.FileUtil;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 1:59
 */
public class NuGetToolsInstallerTest extends BaseTestCase {
  private FeedClient myClient;
  private NuGetToolsInstaller myInstaller;
  private NuGetToolDownloader myDownloader;
  private ToolPaths myPaths;
  private ToolsWatcher myWatcher;
  private NuGetFeedReader myFeed;
  private AvailableToolsState myState;
  private Mockery m;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();
    myClient = m.mock(FeedClient.class);
    myPaths = m.mock(ToolPaths.class);
    myFeed = m.mock(NuGetFeedReader.class);
    myState = m.mock(AvailableToolsState.class);
    myWatcher = m.mock(ToolsWatcher.class);

    myInstaller = new NuGetToolsInstallerImpl(
            myPaths,
            myWatcher);
    myDownloader = new NuGetToolDownloaderImpl(
            myClient,
            myFeed,
            myState,
            myInstaller
    );

    m.checking(new Expectations(){{
      allowing(myPaths).getNuGetToolsPath(); will(returnValue(createTempDir()));
      allowing(myPaths).getNuGetToolsAgentPluginsPath(); will(returnValue(createTempDir()));
      allowing(myPaths).getNuGetToolsPackages(); will(returnValue(createTempDir()));
    }});
  }

  private FeedPackage feedPackage() {
    return new FeedPackage("atomId", new PackageInfo("pkd", "1.2.3.4"), false, "", "download-url");
  }

  @Test
  public void testPackageValidation() throws ToolException, IOException {
    final File testPackage = getNuGetPackageFile();
    myInstaller.validatePackage(testPackage);
  }

  @NotNull
  private File getNuGetPackageFile() throws IOException {
    File home = createTempFile();
    FileUtil.copy(new File("./nuget-tests/testData/nuget/NuGet.CommandLine.1.8.40002.nupkg"), home);
    return home;
  }

  @Test(expectedExceptions = ToolException.class)
  public void testPackageValidataionFailed() throws IOException, ToolException {
    myInstaller.validatePackage(createTempFile(22233));
  }

  @Test
  public void testUploadKnownFile() throws ToolException, IOException {
    m.checking(new Expectations(){{
      oneOf(myWatcher).checkNow();
    }});

    myInstaller.installNuGet("NuGet.1.2.3.nupkg", getNuGetPackageFile());
    Assert.assertTrue(new File(myPaths.getNuGetToolsPackages(), "NuGet.1.2.3.nupkg").isFile());
    m.assertIsSatisfied();
  }

  @Test
  public void testFeedFile() throws ToolException, IOException {
    final FeedPackage fp = feedPackage();
    m.checking(new Expectations(){{
      oneOf(myWatcher).checkNow();
      oneOf(myState).findTool("packageId"); will(returnValue(fp));
      oneOf(myFeed).downloadPackage(with(equal(myClient)), with(equal(fp)), with(any(File.class)));
      will(new CustomAction("fetch file") {
        public Object invoke(Invocation invocation) throws Throwable {
          final File file = (File) invocation.getParameter(2);
          FileUtil.copy(getNuGetPackageFile(), file);
          return null;
        }
      });
    }});

    myDownloader.installNuGet("packageId");
    Assert.assertTrue(new File(myPaths.getNuGetToolsPackages(), "pkd.1.2.3.4.nupkg").isFile());
    m.assertIsSatisfied();
  }

  @Test(expectedExceptions = ToolException.class)
  public void testFeedFile_invalid() throws ToolException, IOException {
    final FeedPackage fp = feedPackage();
    m.checking(new Expectations(){{
      oneOf(myWatcher).checkNow();
      oneOf(myState).findTool("packageId"); will(returnValue(fp));
      oneOf(myFeed).downloadPackage(with(equal(myClient)), with(equal(fp)), with(any(File.class)));
      will(new CustomAction("fetch file") {
        public Object invoke(Invocation invocation) throws Throwable {
          final File file = (File) invocation.getParameter(1);
          FileUtil.writeFileAndReportErrors(file, Strings.EXOTIC);
          return null;
        }
      });
    }});

    myDownloader.installNuGet("packageId");
  }

  @Test(expectedExceptions = ToolException.class)
  public void testFeedFile_downloadFail() throws ToolException, IOException {
    final FeedPackage fp = feedPackage();
    m.checking(new Expectations(){{
      oneOf(myWatcher).checkNow();
      oneOf(myState).findTool("packageId"); will(returnValue(fp));
      oneOf(myFeed).downloadPackage(with(equal(myClient)), with(equal(fp)), with(any(File.class))); will(throwException(new IOException("oops")));
    }});

    myDownloader.installNuGet("packageId");
  }

  @Test(expectedExceptions = ToolException.class)
  public void testFeedFile_noPackage() throws ToolException, IOException {
    m.checking(new Expectations(){{
      oneOf(myWatcher).checkNow();
      oneOf(myState).findTool("packageId"); will(returnValue(null));
    }});

    myDownloader.installNuGet("packageId");
  }
}
