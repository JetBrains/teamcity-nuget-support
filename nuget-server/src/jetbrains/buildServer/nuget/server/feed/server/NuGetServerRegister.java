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

package jetbrains.buildServer.nuget.server.feed.server;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.ToolPaths;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.reader.impl.Param;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SecurityContextEx;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 07.10.11 19:42
 */
public class NuGetServerRegister {
  private static final Logger LOG = Logger.getInstance(NuGetServerRegister.class.getName());

  private final FeedClient myClient;
  private final NuGetServerUri myUris;
  private final ToolPaths myPaths;
  private final SecurityContextEx mySecurityContext;
  private final FeedGetMethodFactory myGetMethodFactory;

  public NuGetServerRegister(@NotNull final FeedClient client,
                             @NotNull final NuGetServerUri uris,
                             @NotNull final ToolPaths paths,
                             @NotNull final SecurityContextEx securityContextEx,
                             @NotNull final FeedGetMethodFactory getMethodFactory) {
    myClient = client;
    myUris = uris;
    myPaths = paths;
    mySecurityContext = securityContextEx;
    myGetMethodFactory = getMethodFactory;
  }

  public void registerPackage(@NotNull final SBuild build, @NotNull final String path) throws NuGetFeedException {
    try {
      mySecurityContext.runAsSystem(new SecurityContextEx.RunAsAction() {
        public void run() throws Throwable {
          registerPackageImpl(build, path);
        }
      });
    } catch (NuGetFeedException e) {
      throw e;
    } catch (Throwable t) {
      ExceptionUtil.rethrowAsRuntimeException(t);
    }
  }


  private void registerPackageImpl(@NotNull final SBuild build, @NotNull final String path) throws NuGetFeedException {
    final BuildArtifact artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getArtifact(path);
    if (artifact == null) {
      LOG.warn("Failed to find artifact " + path + " of build buildId=" + build.getBuildId());
      return;
    }

    final String buildType = build.getBuildTypeId();
    final String buildId = String.valueOf(build.getBuildId());
    final String downloadUrl = "/repository/download/" + build.getBuildTypeId() + "/" + build.getBuildId() + ":id/" + path;
    final String filePath = FileUtil.getRelativePath(myPaths.getArtifactsDirectory(), new File(build.getArtifactsDirectory(), artifact.getRelativePath()));

    if (filePath == null) {
      throw new NuGetFeedException("Failed to map artifact path") {{ setRecoverable(false);}};
    }

    final String addPackageUri = myUris.getAddPackageUri();
    if (addPackageUri == null) {
      throw new NuGetFeedException("Failed to connect to NuGet Feed server. Server is not running");
    }

    final HttpGet get = myGetMethodFactory.createGet(
            addPackageUri,
            new Param("buildType", buildType),
            new Param("buildId", buildId),
            new Param("downloadUrl", downloadUrl),
            new Param("packageFile", filePath)
    );

    final HttpResponse response;
    try {
      response = myClient.execute(get);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new NuGetFeedException("Request was not acceped by server: " + response.getStatusLine().getReasonPhrase());
      }

    } catch (IOException e) {
      throw new NuGetFeedException("Failed to execute request. " + e.getMessage(), e);
    } finally {
      get.abort();
    }
  }
}
