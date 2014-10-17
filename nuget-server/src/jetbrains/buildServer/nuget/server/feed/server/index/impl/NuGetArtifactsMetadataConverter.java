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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.metadata.impl.indexer.MetadataIndexerService;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetArtifactsMetadataConverter {

  private static final Logger LOG = Logger.getInstance(NuGetArtifactsMetadataConverter.class.getName());
  private static final String CONVERSION_MARKER_FILE_NAME = "metadata-converted-marker";

  @NotNull private final MetadataIndexerService myIndexerService;
  @NotNull private final File myDataDir;

  public NuGetArtifactsMetadataConverter(@NotNull final MetadataIndexerService indexerService, @NotNull final File homeDir) {
    myIndexerService = indexerService;
    myDataDir = homeDir;
  }

  public void doConversion() {
    final File dataDirCreated;
    try {
      dataDirCreated = FileUtil.createDir(myDataDir);
    } catch (IOException e) {
      LOG.warn(String.format("Failed to create home directory for %s build metadata conversion marker. Skipping conversion.", NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID), e);
      return;
    }
    if(wasConversionSuccedeed(dataDirCreated))
      LOG.debug(String.format("%s build metadata was already converted. Skipping conversion.", NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID));
    else {
      LOG.info(String.format("Converting %s build metadata.", NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID));
      myIndexerService.reindexProviderData(NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID);
      markConversionAsSuccedeed(dataDirCreated);
    }
  }

  private static boolean wasConversionSuccedeed(File dataDir) {
    return new File(dataDir, CONVERSION_MARKER_FILE_NAME).isFile();
  }

  private static void markConversionAsSuccedeed(File dataDir) {
    try {
      new File(dataDir, CONVERSION_MARKER_FILE_NAME).createNewFile();
    } catch (IOException e) {
      LOG.warn("Failed to mark %s build metadata conversion as succedeed.", e);
    }
    LOG.info(String.format("%s build metadata conversion succedeed.", NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID));
  }
}
