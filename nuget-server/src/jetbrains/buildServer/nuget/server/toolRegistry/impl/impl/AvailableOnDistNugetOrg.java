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
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.http.HttpUtil;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Evgeniy.Koshkin
 */
public class AvailableOnDistNugetOrg implements AvailableToolsFetcher {
  private final static String DIST_NUGET_ORG_INDEX_JSON_URL = "http://dist.nuget.org/index.json";
  private static final Logger LOG = Logger.getInstance(AvailableOnDistNugetOrg.class.getName());

  @NotNull
  public String getSourceDisplayName() {
    return "http://dist.nuget.org";
  }

  @NotNull
  public Collection<NuGetTool> fetchAvailable() {
    HttpClient client = HttpUtil.createHttpClient(60);
    final GetMethod post = new GetMethod(DIST_NUGET_ORG_INDEX_JSON_URL);
    post.addRequestHeader("Accept", "application/json");
    try {
      int status = client.executeMethod(post);
      String respText = post.getResponseBodyAsString();
      if (status == HttpStatus.SC_OK) {
        final Gson gson = new Gson();
        final AvailableArtifacts artifacts = gson.fromJson(respText, AvailableArtifacts.class);
        final List<NuGetTool> nugets = new ArrayList<NuGetTool>();
        for (final Artifact artifact : artifacts.getArtifacts()) {
          if(!artifact.getName().equalsIgnoreCase("win-x86-commandline")) continue;
          for (final Version cmdVersion : artifact.getVersions()) {
            nugets.add(new NuGetTool() {
              @NotNull
              public String getId() {
                return artifact.getName() + cmdVersion.getVersion();
              }

              @NotNull
              public String getVersion() {
                return cmdVersion.getVersion();
              }
            });
          }
        }
        return nugets;
      } else{
        LOG.warn(String.format("Recieved http status %d when fetching available nuget.exe versions from %s", status, DIST_NUGET_ORG_INDEX_JSON_URL));
        return Collections.emptyList();
      }
    } catch (IOException e) {
      LOG.warn("Failed to fetch available nuget.exe versions from " + DIST_NUGET_ORG_INDEX_JSON_URL, e);
      return Collections.emptyList();
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
