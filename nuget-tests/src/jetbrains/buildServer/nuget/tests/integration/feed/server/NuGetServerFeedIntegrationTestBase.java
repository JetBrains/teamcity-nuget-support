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

import jetbrains.buildServer.nuget.server.feed.server.controllers.PackageInfoSerializer;
import jetbrains.buildServer.nuget.server.feed.server.index.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServer;
import jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServerBase;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import java.io.*;
import java.util.*;

import static jetbrains.buildServer.nuget.server.feed.server.PackagesIndex.TEAMCITY_ARTIFACT_RELPATH;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 03.11.11 17:01
 */
public class NuGetServerFeedIntegrationTestBase extends NuGetServerIntegrationTestBase {
  @NotNull
  protected HttpServerHandler packagesFileHandler(@NotNull final File responseFile) throws IOException {
    return new HttpServerHandler() {
      public SimpleHttpServerBase.Response processRequest(@NotNull String requestLine, @Nullable String path) {
        if (!(myHttpContextUrl + "/packages-metadata.html").equals(path)) return null;

        if (checkContainsToken(requestLine)) {
          return SimpleHttpServer.getFileResponse(responseFile, Arrays.asList("Content-Type: text/plain; encoding=UTF-8"));
        } else {
          System.out.println("Failed to find authorization token in request!");
          return SimpleHttpServer.createStreamResponse(SimpleHttpServer.STATUS_LINE_500, Collections.<String>emptyList(), "invalid token".getBytes());
        }
      }
    };
  }

  protected void renderPackagesResponseFile(@NotNull final File responseFile,
                                            @NotNull final File... packagesFile) throws PackageLoadException, IOException {
    final Writer w = new OutputStreamWriter(new FileOutputStream(responseFile), "utf-8");
    w.append("                 ");

    for (final File packageFile : packagesFile) {
      renderPackage(w, packageFile, true, 42L);
    }

    FileUtil.close(w);
    System.out.println("Generated response file: " + responseFile);

    String text = loadAllText(responseFile);
    System.out.println("Generated server response:\r\n" + text);
  }

  private void renderPackage(@NotNull final Writer w,
                             @NotNull final File packageFile,
                             final boolean isLatest,
                             final long buildId) throws PackageLoadException, IOException {
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

    final LocalNuGetPackageItemsFactory factory = new LocalNuGetPackageItemsFactory();
    final Map<String, String> map = new HashMap<String, String>(factory.loadPackage(artifact));
    map.put(TEAMCITY_ARTIFACT_RELPATH, "some/package/download/" + packageFile.getName());

    presentPackageEntry(w, isLatest, build, map);
  }

  private void presentPackageEntry(@NotNull Writer w,
                                   boolean isLatest,
                                   @NotNull final SFinishedBuild build,
                                   @NotNull final Map<String, String> map) throws IOException {
    new PackageInfoSerializer(myPaths).serializePackage(map, build.getBuildTypeId(), build.getBuildId(), isLatest, w);
    w.append("                 ");
  }

}
