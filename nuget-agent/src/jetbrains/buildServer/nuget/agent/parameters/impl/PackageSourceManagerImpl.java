/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.parameters.PackageSourceManager;
import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created 04.01.13 19:19
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class PackageSourceManagerImpl implements PackageSourceManager {
  @NotNull
  public Collection<PackageSource> getGlobalPackageSources(@NotNull AgentRunningBuild build) {
    final Collection<PackageSource> result = new ArrayList<PackageSource>();

    for (AgentBuildFeature feature : build.getBuildFeaturesOfType(PackagesConstants.ATHU_FEATURE_TYPE)) {
      final String feed = feature.getParameters().get(PackagesConstants.NUGET_AUTH_FEED);
      final String user = feature.getParameters().get(PackagesConstants.NUGET_AUTH_USERNAME);
      final String pass = feature.getParameters().get(PackagesConstants.NUGET_AUTH_PASSWORD);

      result.add(source(feed, user, pass));
    }

    final String tcfeed = build.getSharedConfigParameters().get(NuGetServerConstants.FEED_AUTH_REFERENCE);
    if (!StringUtil.isEmptyOrSpaces(tcfeed)) {
      result.add(source(tcfeed, build.getAccessUser(), build.getAccessCode()));
    }

    return result;
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
    };
  }
}
