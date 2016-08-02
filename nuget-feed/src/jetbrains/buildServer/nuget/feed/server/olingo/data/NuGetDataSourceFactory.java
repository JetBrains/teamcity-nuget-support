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

package jetbrains.buildServer.nuget.feed.server.olingo.data;

import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.index.PackagesIndex;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * Creates a new nuget package source.
 */
public class NuGetDataSourceFactory {

  private final PackagesIndex myIndex;
  private final NuGetServerSettings mySettings;

  public NuGetDataSourceFactory(@NotNull final PackagesIndex index, @NotNull final NuGetServerSettings serverSettings) {
    myIndex = index;
    mySettings = serverSettings;
  }

  @NotNull
  public NuGetDataSource create(@NotNull final URI requestUri) {
    return new NuGetDataSource(myIndex, mySettings, requestUri);
  }
}
