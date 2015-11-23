/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.toolRegistry.impl.impl;

import com.google.gson.Gson;
import jetbrains.buildServer.http.HttpUtil;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.server.toolRegistry.FetchException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static jetbrains.buildServer.nuget.common.FeedConstants.EXE_EXTENSION;

/**
 * @author Evgeniy.Koshkin
 */
public class AvailableOnDistNugetOrg implements AvailableToolsFetcher {
  private static final int CONNECTION_TIMEOUT_SECONDS = 60;
  private final static String DIST_NUGET_ORG_INDEX_JSON_URL = "http://dist.nuget.org/index.json";
  private static final String NUGET_COMMANDLINE_ARTIFACT_NAME = "win-x86-commandline";

  @NotNull
  public String getSourceDisplayName() {
    return "http://dist.nuget.org";
  }

  @NotNull
  public Collection<DownloadableNuGetTool> fetchAvailable() throws FetchException {
    HttpClient client = HttpUtil.createHttpClient(CONNECTION_TIMEOUT_SECONDS);
    final GetMethod post = new GetMethod(DIST_NUGET_ORG_INDEX_JSON_URL);
    post.addRequestHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
    try {
      int status = client.executeMethod(post);
      if (status != HttpStatus.SC_OK)
        throw new FetchException(String.format("Recieved http status %d when fetching available nuget.exe versions from %s", status, DIST_NUGET_ORG_INDEX_JSON_URL));

      String respText = post.getResponseBodyAsString();
      final Gson gson = new Gson();
      final AvailableArtifacts artifacts = gson.fromJson(respText, AvailableArtifacts.class);
      final List<DownloadableNuGetTool> nugets = new ArrayList<DownloadableNuGetTool>();
      for (final Artifact artifact : artifacts.getArtifacts()) {
        if(!artifact.getName().equalsIgnoreCase(NUGET_COMMANDLINE_ARTIFACT_NAME)) continue;
        for (final Version commandlineVersion : artifact.getVersions()) {
          nugets.add(new DownloadableNuGetTool() {
            @NotNull
            public String getDownloadUrl() {
              return commandlineVersion.getUrl();
            }

            @NotNull
            public String getDestinationFileName() {
              return FeedConstants.NUGET_COMMANDLINE + "." + commandlineVersion.getVersion() + EXE_EXTENSION;
            }

            @NotNull
            public String getId() {
              return FeedConstants.NUGET_COMMANDLINE + "." + commandlineVersion.getVersion();
            }

            @NotNull
            public String getVersion() {
              return commandlineVersion.getVersion();
            }
          });
        }
      }
      return nugets;
    } catch (IOException e) {
      throw new FetchException("Failed to fetch available nuget.exe versions from " + DIST_NUGET_ORG_INDEX_JSON_URL, e);
    }
  }

  private class AvailableArtifacts{
    private Collection<Artifact> artifacts;

    public Collection<Artifact> getArtifacts() {
      return artifacts;
    }
  }

  private class Artifact {
    private String name;
    private String displayName;
    private Collection<Version> versions;

    public String getName() {
      return name;
    }

    public String getDisplayName() {
      return displayName;
    }

    public Collection<Version> getVersions() {
      return versions;
    }
  }

  private class Version {
    private String displayName;
    private String version;
    private String url;
    private String releasedate;

    public String getDisplayName() {
      return displayName;
    }

    public String getVersion() {
      return version;
    }

    public String getUrl() {
      return url;
    }

    public String getReleaseDate() {
      return releasedate;
    }
  }
}
