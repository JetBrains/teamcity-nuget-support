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

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 1:59
 */
public class NuGetToolsInstallerTest extends BaseTestCase {
  private NuGetToolsInstaller myInstaller;
  private File myToolsPath;
  private File myPluginsPath;
  private ToolPaths myPaths;
  private NuGetFeedReader myFeed;
  private InstallLogger myLogger;
  private AvailableToolsState myState;
  private Mockery m;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();
    myPaths = m.mock(ToolPaths.class);
    myFeed = m.mock(NuGetFeedReader.class);
    myLogger = m.mock(InstallLogger.class);
    myState = m.mock(AvailableToolsState.class);

    myInstaller = new NuGetToolsInstaller(
            myPaths,
            myFeed,
            myState,
            new ToolsWatcher(
                    myPaths,
                    new ToolPacker(),
                    new ToolUnpacker()
            ));

    m.checking(new Expectations(){{
      allowing(myPaths).getTools(); will(returnValue(myToolsPath));
      allowing(myPaths).getAgentPluginsPath(); will(returnValue(myPluginsPath));
    }});
  }

  @Test
  public void test_noPackageFound() {
    m.checking(new Expectations() {{
      oneOf(myState).findTool("pkd"); will(returnValue(null));
      oneOf(myLogger).started("pkd");
      oneOf(myLogger).packageNotFound("pkd");
      oneOf(myLogger).finished(with(equal("pkd")), with(any(FeedPackage.class)));
    }});
    myInstaller.installNuGet("pkd", myLogger);

    m.assertIsSatisfied();
  }

  @Test
  public void test_packageFound() throws IOException {
    final FeedPackage pkd = feedPackage();
    m.checking(new Expectations() {{
      oneOf(myState).findTool("pkd");  will(returnValue(pkd));
      oneOf(myFeed).downloadPackage(with(equal(pkd)), with(any(File.class)));

      oneOf(myLogger).started("pkd");
      oneOf(myLogger).packageDownloadStarted(pkd);
      oneOf(myLogger).packageDownloadFinished(with(equal(pkd)));
      oneOf(myLogger).packageUnpackStarted(with(equal(pkd)), with(any(File.class)));
      oneOf(myLogger).packageUnpackFinished(with(equal(pkd)), with(any(File.class)), with(any(File.class)));

      oneOf(myLogger).agentToolPublishStarted(with(equal(pkd)), with(any(File.class)));
      oneOf(myLogger).agentToolPublishFinished(with(equal(pkd)), with(any(File.class)));

      oneOf(myLogger).agentToolPackStarted(with(equal(pkd)), with(any(File.class)));
      oneOf(myLogger).agentToolPackFinished(with(equal(pkd)));

      oneOf(myLogger).finished(with(equal("pkd")), with(pkd));
    }});
    myInstaller.installNuGet("pkd", myLogger);

    m.assertIsSatisfied();
  }

  private FeedPackage feedPackage() {
    return new FeedPackage("atomId", new PackageInfo("pkd", "1.2.3.4"), false, "", "download-url");
  }
}
