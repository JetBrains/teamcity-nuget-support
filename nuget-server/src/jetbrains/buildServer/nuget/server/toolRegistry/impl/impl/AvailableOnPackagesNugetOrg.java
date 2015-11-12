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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.FetchException;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static jetbrains.buildServer.nuget.common.FeedConstants.*;

/**
 * @author Evgeniy.Koshkin
 */
public class AvailableOnPackagesNugetOrg implements AvailableToolsFetcher {

  private static final Logger LOG = Logger.getInstance(AvailableOnPackagesNugetOrg.class.getName());

  @NotNull private final FeedClient myFeed;
  @NotNull private final NuGetFeedReader myReader;

  public AvailableOnPackagesNugetOrg(@NotNull FeedClient feed, @NotNull NuGetFeedReader reader) {
    myFeed = feed;
    myReader = reader;
  }

  @NotNull
  public String getSourceDisplayName() {
    return "http://packages.nuget.org";
  }

  @NotNull
  public Collection<DownloadableNuGetTool> fetchAvailable() throws FetchException {
    FetchException exception = null;
    for (String feedUrl : Arrays.asList(NUGET_FEED_V2, NUGET_FEED_V1)) {
      try {
        final List<FeedPackage> packages = new ArrayList<FeedPackage>(myReader.queryPackageVersions(myFeed, feedUrl, FeedConstants.NUGET_COMMANDLINE));
        Collections.sort(packages, new Comparator<FeedPackage>() {
          public int compare(@NotNull final FeedPackage o1, @NotNull final FeedPackage o2) {
            return -o1.compareTo(o2);
          }
        });
        return CollectionsUtil.filterAndConvertCollection(
                packages,
                new Converter<DownloadableNuGetTool, FeedPackage>() {
                  public DownloadableNuGetTool createFrom(@NotNull final FeedPackage source) {
                    return new DownloadableNuGetTool() {
                      @NotNull
                      public String getDownloadUrl() {
                        return source.getDownloadUrl();
                      }

                      @NotNull
                      public String getDestinationFileName() {
                        return source.getInfo().getId() + "." + source.getInfo().getVersion() + NUGET_EXTENSION;
                      }

                      @NotNull
                      public String getId() {
                        return source.getInfo().getId() + "." + source.getInfo().getVersion();
                      }

                      @NotNull
                      public String getVersion() {
                        return source.getInfo().getVersion();
                      }
                    };
                  }
                },
                new Filter<FeedPackage>() {
                  public boolean accept(@NotNull FeedPackage data) {
                    final String[] version = data.getInfo().getVersion().split("\\.");
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
      } catch (IOException e) {
        LOG.warn("Failed to fetch versions from: " + feedUrl + ". " + e.getMessage(), e);
        exception = new FetchException(e.getMessage(), e);
      }
    }
    throw exception;
  }
}
