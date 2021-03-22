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

import jetbrains.buildServer.agent.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.11.11 19:05
 */
public class NuGetServerConstants {
  public static final String FEED_REF_PREFIX = "teamcity.nuget.feed.";
  public static final String FEED_REF_URL_SUFFIX = ".url";
  public static final String FEED_REF_PUBLIC_URL_SUFFIX = ".publicUrl";

  public static final String FEED_REFERENCE_AGENT_API_KEY_PROVIDED = "teamcity.nuget.feed.api.key";
  public static final String FEED_INDEXING_ENABLED_PROP = "teamcity.nuget.index.packages";
  public static final String FEED_AGENT_SIDE_INDEXING = "teamcity.nuget.feed.agentSideIndexing";
  public static final String FEED_PARAMETERS_PROVIDER_ENABLE_FALLBACK_PARAMETERS_FOR_RUNNING_BUILD = "nuget.feed.parameters.provider.enable.fallback.for.running.build";
  public static final String NUGET_SERVER_CLI_FORCE_ASSEMBLY_VALIDATION_PROP = "teamcity.nuget.server.cli.force.assembly.validation";
  public static final String NUGET_CLI_FORCE_ASSEMBLY_VALIDATION_ARG = "-v";
  public static final String NUGET_SERVER_CLI_PATH_WHITELIST_PROP = "teamcity.nuget.server.cli.path.witelist";
  public static final String NUGET_SERVER_CLI_PATH_WHITELIST_DEFAULT = "nuget;nuget.exe";

  public static final Pattern FEED_PARAM_AUTH_PATTERN = Pattern.compile(String.format("^%s.*(%s|%s)$",
    escapeDots(Constants.SYSTEM_PREFIX + NuGetServerConstants.FEED_REF_PREFIX),
    escapeDots(NuGetServerConstants.FEED_REF_URL_SUFFIX),
    escapeDots(NuGetServerConstants.FEED_REF_PUBLIC_URL_SUFFIX)
  ));

  public static final Pattern FEED_PARAM_PATTERN = Pattern.compile(String.format("^%s.*\\.v\\d$",
    escapeDots(NuGetServerConstants.FEED_REF_PREFIX)
  ));

  @NotNull
  private static String escapeDots(@NotNull final String value) {
    return value.replace(".", "\\.");
  }
}
