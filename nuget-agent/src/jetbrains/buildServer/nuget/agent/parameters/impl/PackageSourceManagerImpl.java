/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.agent.parameters.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.nuget.agent.parameters.PackageSourceManager;
import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created 04.01.13 19:19
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class PackageSourceManagerImpl implements PackageSourceManager {

  private static final Logger LOG = Logger.getInstance(PackageSourceManagerImpl.class.getName());

  @NotNull
  public Set<PackageSource> getGlobalPackageSources(@NotNull AgentRunningBuild build) {
    final Map<String, PackageSource> result = new HashMap<String, PackageSource>();

    for (AgentBuildFeature feature : build.getBuildFeaturesOfType(PackagesConstants.AUTH_FEATURE_TYPE)) {
      final String feed = feature.getParameters().get(PackagesConstants.NUGET_AUTH_FEED);
      final String user = feature.getParameters().get(PackagesConstants.NUGET_AUTH_USERNAME);
      final String pass = feature.getParameters().get(PackagesConstants.NUGET_AUTH_PASSWORD);

      result.put(normalizeUrl(feed), source(feed, user, pass));
    }

    final Map<String, String> parameters = build.getSharedConfigParameters();
    for (String key : parameters.keySet()) {
      if (NuGetServerConstants.FEED_URL_PATTERN.matcher(key).find() ||
          NuGetServerConstants.FEED_REF_HTTP_AUTH_GLOBAL.equals(key) ||
          NuGetServerConstants.FEED_REF_HTTP_AUTH_PUBLIC_GLOBAL.equals(key)) {
        final String feedUrl = parameters.get(key);
        if (StringUtil.isEmptyOrSpaces(feedUrl)) {
          LOG.debug("Failed to resolve TeamCity NuGet feed url via config parameter " + key);
        } else {
          result.put(normalizeUrl(feedUrl), source(feedUrl, build.getAccessUser(), build.getAccessCode()));
        }
      }
    }

    return new HashSet<PackageSource>(result.values());
  }

  @NotNull
  private String normalizeUrl(@NotNull String url) {
    try {
      return StringUtil.trimEnd(URI.create(url).normalize().toString(), "/");
    } catch (Exception e) {
      return url; // probably not URL, use as is
    }
  }

  @NotNull
  private PackageSource source(@NotNull final String feed,
                               @Nullable final String user,
                               @Nullable final String pass) {
    return new PackageSource() {
      @NotNull
      public String getSource() {
        return feed;
      }

      @Nullable
      public String getUsername() {
        return user;
      }

      @Nullable
      public String getPassword() {
        return pass;
      }

      @Override
      public int hashCode() {
        return feed.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PackageSource)) return false;
        PackageSource that = (PackageSource) obj;
        return feed.equals(that.getSource());
      }
    };
  }
}
