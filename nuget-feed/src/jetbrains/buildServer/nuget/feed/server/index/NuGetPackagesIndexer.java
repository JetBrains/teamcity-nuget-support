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

package jetbrains.buildServer.nuget.feed.server.index;

import jetbrains.buildServer.nuget.common.index.PackageConstants;
import jetbrains.buildServer.serverSide.metadata.impl.indexer.MetadataIndexerService;
import org.jetbrains.annotations.NotNull;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackagesIndexer {

  @NotNull
  private final MetadataIndexerService myMetadataIndexerService;

  public NuGetPackagesIndexer(@NotNull final MetadataIndexerService metadataIndexerService) {
    myMetadataIndexerService = metadataIndexerService;
  }

  /**
   * Re-index all NuGet-related data
   */
  public void reindexAll() {
    myMetadataIndexerService.reindexProviderData(PackageConstants.NUGET_PROVIDER_ID);
  }
}
