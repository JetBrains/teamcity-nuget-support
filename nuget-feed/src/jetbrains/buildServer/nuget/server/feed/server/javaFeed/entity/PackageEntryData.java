/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity;

import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.core.OAtomStreamEntity;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 28.01.12 0:48
 */
public abstract class PackageEntryData extends PackageEntityAdapter implements OAtomStreamEntity {
  private final NuGetIndexEntry myEntry;

  public PackageEntryData(@NotNull final NuGetIndexEntry entry) {
    myEntry = entry;
  }

  public String getAtomEntityType() {
    return "application/zip";
  }

  @Override
  protected String getValue(@NotNull String key) {
    return myEntry.getAttributes().get(key);
  }

  @Nullable
  public String getAtomEntitySource(String baseUri) {
    return resolveUrl(baseUri, myEntry.getPackageDownloadUrl());
  }

  @Nullable
  protected abstract String resolveUrl(@NotNull String baseUrl, @NotNull String url);
}