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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.NetworkUtil;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.server.feed.server.impl.NuGetServerSettingsImpl;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackagesIndexImpl;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.transform.DownloadUrlComputationTransformation;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.transform.IsPrereleaseTransformation;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.NuGetProducerHolder;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.functions.NuGetFeedFunctions;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static jetbrains.buildServer.nuget.server.feed.server.index.impl.NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 04.01.12 23:55
 */
public class NuGetJavaFeedIntegrationTestBase extends NuGetFeedIntegrationTestBase {
  protected NuGetProducerHolder myProducer;
  private int myPort;
  private List<NuGetIndexEntry> myFeed;
  protected PackagesIndex myIndex;
  protected PackagesIndex myActualIndex;
  protected PackagesIndex myIndexProxy;
  protected MetadataStorage myMetadataStorage;
  private ODataServer myServer;
  private int myCount;
  private NuGetServerSettings mySettings;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCount = 0;
    myPort = NetworkUtil.getFreePort(14444);
    myFeed = new ArrayList<NuGetIndexEntry>();
    myIndex = m.mock(PackagesIndex.class);
    myActualIndex = myIndex;
    myIndexProxy = m.mock(PackagesIndex.class, "proxy");
    mySettings = m.mock(NuGetServerSettings.class);
    myMetadataStorage = m.mock(MetadataStorage.class);
    m.checking(new Expectations() {{
      allowing(myIndexProxy).getNuGetEntries(); will(new CustomAction("lazy return packages") {
        public Object invoke(Invocation invocation) throws Throwable {
          return getPackages();
        }
      });
      allowing(myIndex).getNuGetEntries(); will(returnIterator(myFeed));
      allowing(mySettings).getNuGetFeedControllerPath(); will(returnValue(NuGetServerSettingsImpl.PATH));

      allowing(myMetadataStorage).getAllEntries(NUGET_PROVIDER_ID); will(new CustomAction("transform entries") {
        public Object invoke(Invocation invocation) throws Throwable {
          return toEntries().iterator();
        }
      });
    }});
    myProducer = new NuGetProducerHolder(myIndexProxy, mySettings, new NuGetFeedFunctions(myIndexProxy));

    startNuGetFeedServer();
  }

  @NotNull
  private Collection<BuildMetadataEntry> toEntries() {
    Collection<BuildMetadataEntry> ee = new ArrayList<BuildMetadataEntry>();
    for (final NuGetIndexEntry e : myFeed) {
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

  public void enablePackagesIndexSorting() {
    setPackagesIndex(new PackagesIndexImpl(
            myMetadataStorage,
            Arrays.asList(
              new IsPrereleaseTransformation(),
              new MockExternalIdTransformation(),
              new DownloadUrlComputationTransformation(mySettings)
            )
    ));
  }

  @NotNull
  private Iterator<NuGetIndexEntry> getPackages() {
    return myActualIndex.getNuGetEntries();
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    myServer.stop();
  }

  @NotNull
  protected String getServerBase() {
    return "http://localhost:" + myPort;
  }

  @Override
  protected String getNuGetServerUrl() {
    return getServerBase() + NuGetServerSettingsImpl.PATH + "/";
  }

  protected void startNuGetFeedServer() {
    // register the producer as the static instance, then launch the http server
    final ODataProducer producer = myProducer.getProducer();
    DefaultODataProducerProvider.setInstance(producer);
    myServer = ODataProducerUtil.hostODataServer(getNuGetServerUrl());
  }

  protected NuGetIndexEntry addPackage(@NotNull final File file, boolean isLatest) throws IOException {
    final Map<String, String> map = indexPackage(file, isLatest);
    NuGetIndexEntry e = new NuGetIndexEntry(file.getName(), map);
    myFeed.add(e);
    return e;
  }

  @NotNull
  private Map<String, String> indexPackage(@NotNull final File file, final boolean isLatest) throws IOException {
    final int buildId = myCount++;
    final Map<String,String> map = indexPackage(file, isLatest, buildId);
    map.put(PackagesIndex.TEAMCITY_DOWNLOAD_URL, "/downlaodREpoCon/downlaod-url");
    return map;
  }

  protected NuGetIndexEntry addMockPackage(@NotNull final NuGetIndexEntry entry, boolean isLatest) {
    final Map<String, String> map = new HashMap<String, String>(entry.getAttributes());
    final int buildId = myCount++;

    final String id = entry.getAttributes().get("Id");
    final String ver = entry.getAttributes().get("Version");

    map.put("Version", ver + "." + myCount);
    map.put("IsLatestVersion", String.valueOf(isLatest));
    map.put("IsAbsoluteLatestVersion", String.valueOf(isLatest));
    map.put(PackagesIndex.TEAMCITY_DOWNLOAD_URL, "/downlaodREpoCon/downlaod-url");
    NuGetIndexEntry e = new NuGetIndexEntry(id + "." + ver, map);
    myFeed.add(e);
    return e;
  }

  @NotNull
  protected NuGetIndexEntry addMockPackage(@NotNull final String id, @NotNull final String ver) throws IOException {
    final Map<String, String> map = new TreeMap<String, String>(indexPackage(Paths.getTestDataPath("packages/NuGet.Core.1.5.20902.9026.nupkg"), true));

    map.put("Id", id);
    map.put("Version", ver);

    map.remove("IsLatestVersion");
    map.remove("IsAbsoluteLatestVersion");
    map.put(PackagesIndex.TEAMCITY_DOWNLOAD_URL, "/downlaodREpoCon/downlaod-url");
    NuGetIndexEntry e = new NuGetIndexEntry(id + "." + ver, map);
    myFeed.add(e);
    return e;
  }


  protected void dumpFeed() {
    final Iterator<NuGetIndexEntry> entries = myIndexProxy.getNuGetEntries();
    while(entries.hasNext()) {
      final NuGetIndexEntry e = entries.next();
      final Map<String,String> a = e.getAttributes();
      System.out.println(a.get("Id") + " " + a.get("Version") + " => absolute:" + a.get("IsAbsoluteLatestVersion") + ", latest: " + a.get("IsLatestVersion") + ", prerelease: " + a.get("IsPrerelease"));
    }
  }

}
