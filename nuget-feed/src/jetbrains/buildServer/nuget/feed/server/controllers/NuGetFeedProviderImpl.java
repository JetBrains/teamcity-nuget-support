/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.controllers;

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.odata4j.ODataRequestHandler;
import jetbrains.buildServer.nuget.feed.server.olingo.OlingoRequestHandler;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a concrete NuGet feed handler.
 */
public class NuGetFeedProviderImpl implements NuGetFeedProvider {

  private final ODataRequestHandler myODataRequestHandler;
  private final OlingoRequestHandler myOlingoRequestHandler;

  public NuGetFeedProviderImpl(@NotNull final ODataRequestHandler oDataRequestHandler,
                               @NotNull final OlingoRequestHandler olingoRequestHandler) {

    myODataRequestHandler = oDataRequestHandler;
    myOlingoRequestHandler = olingoRequestHandler;
  }

  @Override
  @NotNull
  public NuGetFeedHandler getHandler() {
    if (TeamCityProperties.getBooleanOrTrue(NuGetFeedConstants.PROP_NUGET_FEED_NEW_SERIALIZER)) {
      return myOlingoRequestHandler;
    } else {
      return myODataRequestHandler;
    }
  }
}
