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
import jetbrains.buildServer.nuget.server.feed.server.index.PackageLoadException;
import jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServer;
import jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServerBase;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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
    final Map<String, String> map = indexPackage(packageFile, isLatest, buildId);

    new PackageInfoSerializer().serializePackage(map, w);
    w.append("                 ");
  }


}
