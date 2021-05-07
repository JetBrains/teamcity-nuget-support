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

import org.jetbrains.annotations.NotNull;

/**
 * NuGet feed constants.
 */
public class NuGetFeedConstants {
  public static final String PROP_NUGET_API_VERSION = "teamcity.nuget.api.version";
  public static final String PROP_NUGET_FEED_NEW_SERIALIZER = "teamcity.nuget.feed.new.serializer";
  public static final String PROP_NUGET_FEED_FILTER_TARGETFRAMEWORK = "teamcity.nuget.feed.filter.targetframework";
  public static final String PROP_NUGET_FEED_PUBLISH_PATH = "teamcity.nuget.feed.publish.path";
  public static final String PROP_NUGET_FEED_USE_CACHE = "teamcity.nuget.feed.use.cache";
  public static final String PROP_NUGET_FEED_CACHE_SIZE = "teamcity.nuget.feed.response.cache.size";
  public static final String PROP_NUGET_FEED_ENABLED = "teamcity.nuget.feed.enabled";
  public static final String PROP_NUGET_FEED_CACHED_SERVLETS = "teamcity.nuget.feed.cached.servlets";
  public static final String PROP_NUGET_FEED_MAX_REQUESTS = "teamcity.nuget.feed.requests.maxRequests";
  public static final String PROP_NUGET_FEED_REQUEST_PENDING_PROCESSING_TIMEOUT = "teamcity.nuget.feed.requests.pending.processing.timeout";
  public static final String PROP_NUGET_FEED_ASYNC_REQUEST_QUEUE_CAPACITY = "teamcity.nuget.feed.async.request.queue.capacity";
  public static final String PROP_NUGET_FEED_ASYNC_REQUEST_ENABLED = "teamcity.nuget.feed.async.request.enabled";
  public static final String PROP_NUGET_FEED_ASYNC_REQUEST_TIMOEUT = "teamcity.nuget.feed.async.request.timeout";

  public static final String NUGET_FEED_API_VERSION = "teamcity.nuget.feed.apiVersion";
  public static final int NUGET_FEED_PACKAGE_SIZE = 100;
  public static final int NUGET_FEED_MAX_REQUESTS = 20;
  public static final Long NUGET_FEED_REQUEST_PENDING_PROCESSING_TIMEOUT = 60L;
  public static final int NUGET_FEED_REQUEST_QUEUE_CAPACITY = 100;

  public static final String NUGET_INDEXER_TYPE = "NuGetPackagesIndexer";
  public static final String NUGET_INDEXER_FEED = "feed";

  public static final String NUGET_FEED_ASYNC_V3 = "nuget/v3/";
  public static final String NUGET_FEED_ASYNC_V3_PACKAGE_REGISTRATIONS = NUGET_FEED_ASYNC_V3 + "registrations";
  public static final String NUGET_FEED_ASYNC_V3_PACKAGE_SEARCH = NUGET_FEED_ASYNC_V3 + "search";
  public static final String NUGET_FEED_ASYNC_V3_PACKAGE_VERSIONS = NUGET_FEED_ASYNC_V3 + "versions";
  public static final String NUGET_FEED_ASYNC_V3_PACKAGE_CONTENT = NUGET_FEED_ASYNC_V3 + "content";
  public static final String NUGET_FEED_ASYNC_DATA_CONTEXT = "nuget.feed.json.data";

  @NotNull
  public String getFeed() {
      return NUGET_INDEXER_FEED;
  }
}
