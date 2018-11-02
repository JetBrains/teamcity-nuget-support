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

package jetbrains.buildServer.nuget.feed.server;

import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.nuget.common.version.VersionUtility;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * NuGet utilities.
 */
public class NuGetUtils {
  @Nullable
  public static String getValue(@NotNull final Map<String, String> attributes, @NotNull final String key) {
    String value = attributes.get(key);
    if (value == null) {
      return null;
    }

    final StringBuilder valueBuilder = new StringBuilder();
    int index = 1;
    while (value != null) {
      valueBuilder.append(value);
      value = attributes.get(key + index++);
    }

    return valueBuilder.toString();
  }

  @NotNull
  public static String getPackageKey(@NotNull final String id, @NotNull final String version) {
    return String.format("%s.%s", id, VersionUtility.normalizeVersion(version)).toLowerCase();
  }

  /**
   * @return context based path of nuget feed OData service
   */
  @NotNull
  public static String getProjectFeedPath(@NotNull final String projectId,
                                          @NotNull final String name,
                                          @NotNull final NuGetAPIVersion version) {
    final String apiPath = String.format(NuGetServerSettings.PROJECT_PATH + "/%s/%s/%s", projectId, name, version.name().toLowerCase());
    if (version == NuGetAPIVersion.V3) {
      return apiPath + "/index.json";
    }
    return apiPath;
  }

  /**
   * @return common path for project nuget feeds.
   */
  @NotNull
  public static String getProjectFeedPath(@NotNull final String projectId,
                                          @NotNull final String name) {
    return String.format(NuGetServerSettings.PROJECT_PATH + "/%s/%s/", projectId, name);
  }

  /**
   * @return context based path of nuget feed OData service
   */
  @NotNull
  public static List<String> getProjectFeedPaths(@NotNull final String projectId,
                                                 @NotNull final String name) {
    return CollectionsUtil.convertCollection(
      Arrays.asList(NuGetAPIVersion.values()),
      apiVersion -> getProjectFeedPath(projectId, name, apiVersion)
    );
  }

  @Nullable
  public static Pair<String, String> feedIdToData(@NotNull String feedId) {
    final List<String> parts = StringUtil.split(feedId, "/");
    if (parts.size() == 1) {
      return new Pair<>(parts.get(0), NuGetFeedData.DEFAULT_FEED_ID);
    } else if (parts.size() == 2) {
      return new Pair<>(parts.get(0), parts.get(1));
    }
    return null;
  }

  /**
   * @return a reference on project feed with selected authType.
   */
  @NotNull
  public static String getProjectFeedReference(@NotNull final String authType,
                                               @NotNull final String projectId,
                                               @NotNull final String name,
                                               @NotNull final NuGetAPIVersion version) {
    return String.format("%s%s.%s.%s.%s",
      NuGetServerConstants.FEED_REF_PREFIX,
      authType,
      projectId,
      name,
      version.name().toLowerCase()
    );
  }
}
