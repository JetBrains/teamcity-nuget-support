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
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;

import java.io.File;

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
  private AvailableToolsState myState;
  private Mockery m;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();
    myPaths = m.mock(ToolPaths.class);
    myFeed = m.mock(NuGetFeedReader.class);
    myState = m.mock(AvailableToolsState.class);

    myInstaller = new NuGetToolsInstaller(
            myPaths,
            myFeed,
            myState,
            new ToolsWatcherImpl(
                    myPaths,
                    new ToolPacker(),
                    new ToolUnpacker()
            ));

    m.checking(new Expectations(){{
      allowing(myPaths).getNuGetToolsPath(); will(returnValue(myToolsPath));
      allowing(myPaths).getNuGetToolsAgentPluginsPath(); will(returnValue(myPluginsPath));
    }});
  }

  private FeedPackage feedPackage() {
    return new FeedPackage("atomId", new PackageInfo("pkd", "1.2.3.4"), false, "", "download-url");
  }
}
