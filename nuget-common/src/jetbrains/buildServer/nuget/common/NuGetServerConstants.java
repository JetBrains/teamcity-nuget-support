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

package jetbrains.buildServer.nuget.common;

import java.util.regex.Pattern;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.11.11 19:05
 */
public class NuGetServerConstants {
  public static final String FEED_REF_PREFIX = "teamcity.nuget.feed.";
  public static final String FEED_REF_GUEST_AUTH_GLOBAL = FEED_REF_PREFIX + "server";
  public static final String FEED_REF_HTTP_AUTH_GLOBAL = FEED_REF_PREFIX + "auth.server";
  public static final String FEED_REF_HTTP_AUTH_PUBLIC_GLOBAL = FEED_REF_PREFIX + "auth.serverRootUrlBased.server";
  public static final String FEED_REF_URL_SUFFIX = ".url";
  public static final String FEED_REF_PUBLIC_URL_SUFFIX = ".publicUrl";

  public static final String FEED_REFERENCE_AGENT_API_KEY_PROVIDED = "teamcity.nuget.feed.api.key";
  public static final String FEED_INDEXING_ENABLED_PROP = "teamcity.nuget.index.packages";
  public static final String FEED_AGENT_SIDE_INDEXING = "teamcity.nuget.feed.agentSideIndexing";

  public static final Pattern FEED_URL_PATTERN = Pattern.compile(String.format("^%s.*(%s|%s)$",
      NuGetServerConstants.FEED_REF_PREFIX.replace(".", "\\."),
      NuGetServerConstants.FEED_REF_URL_SUFFIX.replace(".", "\\."),
      NuGetServerConstants.FEED_REF_PUBLIC_URL_SUFFIX.replace(".", "\\.")
  ));
}
