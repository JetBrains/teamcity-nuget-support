/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.metadata.impl.indexer.MetadataIndexerService;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetArtifactsMetadataConversionManager {
  public NuGetArtifactsMetadataConversionManager(@NotNull final MetadataIndexerService indexerService,
                                                 @NotNull final ServerPaths serverPaths,
                                                 @NotNull final EventDispatcher<BuildServerListener> dispatcher) {
    final File pluginDataDir = new File(serverPaths.getPluginDataDirectory(), NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID);
    final NuGetArtifactsMetadataConverter converter = new NuGetArtifactsMetadataConverter(indexerService, pluginDataDir);
    dispatcher.addListener(new BuildServerAdapter(){
      @Override
      public void serverStartup() {
        converter.doConversion();
      }
    });
  }
}
