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

package jetbrains.buildServer.nuget.standalone.server;

import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.NuGetProducerBase;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntity;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntryData;
import org.jetbrains.annotations.NotNull;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 29.01.12 23:42
*/
public class NuGetApplicationProducer extends NuGetProducerBase {
  public NuGetApplicationProducer(@NotNull final ServerSettings settings) {
    super(new NuGetApplicationPackagesIndex(settings));
  }

  @Override
  protected PackageEntity createODataPackageEntry(@NotNull final NuGetIndexEntry internal) {
    return new PackageEntryData(internal) {
      @Override
      protected String resolveUrl(@NotNull String baseUrl, @NotNull String url) {
        return baseUrl + url;
      }
    };
  }

}
