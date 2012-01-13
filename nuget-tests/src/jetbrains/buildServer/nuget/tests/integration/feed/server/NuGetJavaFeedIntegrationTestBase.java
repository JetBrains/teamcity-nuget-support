/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import jetbrains.buildServer.NetworkUtil;
import jetbrains.buildServer.nuget.server.feed.server.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.controllers.NuGetProducer;
import jetbrains.buildServer.nuget.server.feed.server.controllers.PackagesFeedController;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 04.01.12 23:55
 */
public class NuGetJavaFeedIntegrationTestBase extends NuGetFeedIntegrationTestBase {
  protected NuGetProducer myProducer;
  private int myPort;
  private List<NuGetIndexEntry> myFeed;
  private PackagesIndex myIndex;
  private ODataServer myServer;
  private int myCount;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCount = 0;
    myPort = NetworkUtil.getFreePort(14444);
    myFeed = new ArrayList<NuGetIndexEntry>();
    myIndex = m.mock(PackagesIndex.class);
    m.checking(new Expectations() {{
      allowing(myIndex).getNuGetEntries();
      will(returnIterator(myFeed));
    }});
    myProducer = new NuGetProducer(myIndex);

    startNuGetFeedServer();
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    myServer.stop();
  }

  @Override
  protected String getNuGetServerUrl() {
    return "http://localhost:" + myPort + PackagesFeedController.PATH + "/";
  }

  protected void startNuGetFeedServer() {
    // register the producer as the static instance, then launch the http server
    final ODataProducer producer = myProducer.getProducer();
    DefaultODataProducerProvider.setInstance(producer);
    myServer = ODataProducerUtil.hostODataServer(getNuGetServerUrl());
  }

  protected void addPackage(@NotNull final File file, boolean isLatest) throws IOException {
    final int buildId = myCount++;
    final Map<String,String> map = indexPackage(file, isLatest, buildId);
    myFeed.add(new NuGetIndexEntry(file.getName(), map, "bt-xx" + buildId, buildId, "/downlaodREpoCon/downlaod-url"));
  }

}
