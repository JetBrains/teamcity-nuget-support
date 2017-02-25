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

package jetbrains.buildServer.nuget.server.tool.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedClient;
import jetbrains.buildServer.nuget.feedReader.NuGetPackage;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedReader;
import jetbrains.buildServer.tools.available.AvailableToolsFetcher;
import jetbrains.buildServer.tools.available.DownloadableToolVersion;
import jetbrains.buildServer.tools.available.FetchAvailableToolsResult;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static jetbrains.buildServer.nuget.common.FeedConstants.*;

/**
 * @author Evgeniy.Koshkin
 */
public class AvailableOnPackagesNugetOrg implements AvailableToolsFetcher {

  private static final Logger LOG = Logger.getInstance(AvailableOnPackagesNugetOrg.class.getName());

  @NotNull private final NuGetFeedClient myFeed;
  @NotNull private final NuGetFeedReader myReader;

  public AvailableOnPackagesNugetOrg(@NotNull NuGetFeedClient feed, @NotNull NuGetFeedReader reader) {
    myFeed = feed;
    myReader = reader;
  }

  @NotNull
  public FetchAvailableToolsResult fetchAvailable() {
    FetchAvailableToolsResult error = null;
    for (String feedUrl : Arrays.asList(NUGET_FEED_V2, NUGET_FEED_V1)) {
      try {
        final Collection<DownloadableToolVersion> fetchedTools = CollectionsUtil.filterAndConvertCollection(
                myReader.queryPackageVersions(myFeed, feedUrl, FeedConstants.NUGET_COMMANDLINE),
                new Converter<DownloadableToolVersion, NuGetPackage>() {
                  public DownloadableToolVersion createFrom(@NotNull final NuGetPackage source) {
                    return new DownloadableNuGetTool(
                            source.getPackageVersion(),
                            source.getDownloadUrl(),
                            source.getPackageId() + "." + source.getPackageVersion() + NUGET_EXTENSION);
                  }
                }, new Filter<NuGetPackage>() {
                  public boolean accept(@NotNull NuGetPackage data) {
                    final String[] version = data.getPackageVersion().split("\\.");
                    if (version.length < 2) return false;
                    int major = parse(version[0]);
                    if (major < 1) return false;

                    int minor = parse(version[1]);
                    return !(major == 1 && minor < 4);

                  }

                  private int parse(String s) {
                    try {
                      return Integer.parseInt(s.trim());
                    } catch (Exception e) {
                      return -1;
                    }
                  }
                }
        );
        return FetchAvailableToolsResult.createSuccessfull(fetchedTools);
      } catch (IOException e) {
        LOG.debug(e);
        error = FetchAvailableToolsResult.createError("Failed to fetch versions from: " + feedUrl, e);
      }
    }
    return error;
  }
}
