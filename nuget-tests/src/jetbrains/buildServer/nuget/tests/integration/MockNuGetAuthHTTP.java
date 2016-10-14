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

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.nuget.feedReader.NuGetFeedCredentials;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.HttpAuthServer;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created 02.01.13 15:36
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class MockNuGetAuthHTTP {
  private HttpAuthServer myHttp;
  private String mySourceUrl;
  private String myDownloadUrl;
  private String myUsername;
  private String myPassword;
  private AtomicBoolean myIsAuthorized;

  @NotNull
  public String getSourceUrl() {
    return mySourceUrl;
  }

  @NotNull
  public String getPackageId() {
    return "FineCollection";
  }

  @NotNull
  public String getDownloadUrl() {
    return myDownloadUrl;
  }

  @NotNull
  public NuGetFeedCredentials getCredentials() {
    return new NuGetFeedCredentials(getUsername(),  getPassword());
  }

  @NotNull
  public String getUsername() {
    return myUsername;
  }

  @NotNull
  public String getPassword() {
    return myPassword;
  }

  @NotNull
  public AtomicBoolean getIsAuthorized() {
    return myIsAuthorized;
  }

  public void stop() {
    if (myHttp != null) {
      myHttp.stop();
      myHttp = null;
    }
  }

  public void start() throws IOException {
    myUsername = "u-" + StringUtil.generateUniqueHash();
    myPassword = "p-" + StringUtil.generateUniqueHash();
    myIsAuthorized = new AtomicBoolean(false);

    myHttp = new HttpAuthServer() {
      @Override
      protected Response getAuthorizedResponse(String request) throws IOException {
        final String path = getRequestPath(request);
        if (path == null) return createStreamResponse(STATUS_LINE_404, Collections.<String>emptyList(), "Not found".getBytes("utf-8"));
        log("NuGet request path: " + path);

        final List<String> xml = Arrays.asList("DataServiceVersion: 1.0;", "Content-Type: application/xml;charset=utf-8");
        final List<String> atom = Arrays.asList("DataServiceVersion: 2.0;", "Content-Type: application/atom+xml;charset=utf-8");

        if (path.endsWith("$metadata")) {
          return createStringResponse(STATUS_LINE_200, xml, loadMockODataFiles("feed/mock/feed.metadata.xml"));
        }

        if (path.contains("nuget/Packages()") && path.contains("?$filter=")) {
          return createStringResponse(STATUS_LINE_200, atom, loadMockODataFiles("feed/mock/feed.package.xml"));
        }

        if (path.contains("nuget/Packages")) {
          return createStringResponse(STATUS_LINE_200, atom, loadMockODataFiles("feed/mock/feed.packages.xml"));
        }

        if (path.contains("nuget")) {
          return createStringResponse(STATUS_LINE_200, xml, loadMockODataFiles("feed/mock/feed.root.xml"));
        }

        if (path.contains("FineCollection.1.0.189.152.nupkg")) {
          return getFileResponse(Paths.getTestDataPath("feed/mock/FineCollection.1.0.189.152.nupkg"), Arrays.asList("Content-Type: application/zip"));
        }

        return createStringResponse(STATUS_LINE_404, Collections.<String>emptyList(), "Not found");
      }

      @NotNull
      @Override
      protected Response getNotAuthorizedResponse(String request) {
        log("Not authorized: " + request);
        return super.getNotAuthorizedResponse(request);
      }

      @Override
      protected boolean authorizeUser(@NotNull String loginPassword) {
        if ((myUsername + ":" + myPassword).equals(loginPassword)) {
          myIsAuthorized.set(true);
          log("Authorized user with password: " + loginPassword);
          return true;
        }
        log("Can't authorize user. Password is incorrect.");
        return false;
      }

      private String loadMockODataFiles(@NotNull String name) throws IOException {
        String source = loadFileUTF8(name);
        source = source.replace("http://buildserver.labs.intellij.net/httpAuth/app/nuget/v1/FeedService.svc/", mySourceUrl);
        source = source.replace("http://buildserver.labs.intellij.net/httpAuth/repository/download/", myDownloadUrl);
        source = source.replaceAll("xml:base=\".*\"", "xml:base=\"" + mySourceUrl + "\"");
        return source;
      }

      @NotNull
      private String loadFileUTF8(@NotNull String name) throws IOException {
        File file = Paths.getTestDataPath(name);
        return loadFileUTF8(file);
      }

      private String loadFileUTF8(@NotNull File file) throws IOException {
        final InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
          final Reader rdr = new InputStreamReader(is, "utf-8");
          StringBuilder sb = new StringBuilder();
          int ch;
          while ((ch = rdr.read()) >= 0) {
            sb.append((char) ch);
          }
          return sb.toString();
        } finally {
          FileUtil.close(is);
        }
      }
    };

    myHttp.start();
    mySourceUrl = "http://localhost:" + myHttp.getPort() + "/nuget/";
    myDownloadUrl = "http://localhost:" + myHttp.getPort() + "/download/";
  }

  private void log(String message) {
    System.out.println("[mock feed] " + message);
  }

  public static interface Action {
    void runTest(@NotNull MockNuGetAuthHTTP http) throws Throwable;
  }

  public static void executeTest(@NotNull Action action) throws Throwable {
    MockNuGetAuthHTTP http = new MockNuGetAuthHTTP();
    http.start();
    try {
      action.runTest(http);
    } finally {
      http.stop();
    }
  }

}
