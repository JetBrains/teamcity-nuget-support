

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
