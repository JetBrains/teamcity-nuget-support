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

import jetbrains.buildServer.nuget.server.feed.server.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static jetbrains.buildServer.nuget.server.feed.server.PackagesIndex.TEAMCITY_ARTIFACT_RELPATH;
import static jetbrains.buildServer.nuget.server.feed.server.PackagesIndex.TEAMCITY_BUILD_TYPE_ID;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.01.12 0:57
 */
public class NuGetFeedIntegrationTestBase extends IntegrationTestBase {
  protected Collection<InputStream> myStreams;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myStreams = new ArrayList<InputStream>();
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    for (InputStream stream : myStreams) {
      FileUtil.close(stream);
    }
  }

  @NotNull
  protected Map<String, String> indexPackage(@NotNull final File packageFile,
                                             final boolean isLatest,
                                             final long buildId) throws IOException {
    final SFinishedBuild build = m.mock(SFinishedBuild.class, "build-" + packageFile.getPath());
    final BuildArtifact artifact = m.mock(BuildArtifact.class, "artifact-" + packageFile.getPath());

    m.checking(new Expectations() {{
      allowing(build).getBuildId(); will(returnValue(buildId));
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

    try {
      final LocalNuGetPackageItemsFactory factory = new LocalNuGetPackageItemsFactory();
      final Map<String, String> map = new HashMap<String, String>(factory.loadPackage(artifact, new Date()));
      map.put(TEAMCITY_ARTIFACT_RELPATH, "some/package/download/" + packageFile.getName());
      map.put(TEAMCITY_BUILD_TYPE_ID, "bt_" + packageFile.getName());
      map.put("TeamCityDownloadUrl", "some-download-url/" + packageFile.getName());
      map.put("IsLatestVersion", String.valueOf(isLatest));
      return map;
    } catch (PackageLoadException e) {
      throw new RuntimeException(e);
    }
  }

}
