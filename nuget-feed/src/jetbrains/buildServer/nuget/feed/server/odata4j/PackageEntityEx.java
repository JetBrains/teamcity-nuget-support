

package jetbrains.buildServer.nuget.feed.server.odata4j;

import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.NuGetUtils;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.odata4j.entity.PackageEntityAdapter;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OAtomStreamEntity;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.01.12 12:40
 */
public class PackageEntityEx extends PackageEntityAdapter implements OAtomStreamEntity {

  private static final String APPLICATION_ZIP = "application/zip";

  private final NuGetIndexEntry myEntry;

  public PackageEntityEx(@NotNull final NuGetIndexEntry entry) {
    myEntry = entry;
  }

  public String getAtomEntityType() {
    return APPLICATION_ZIP;
  }

  public String getAtomEntitySource(String baseUri) {
    int idx = baseUri.indexOf(NuGetServerSettings.PROJECT_PATH);
    if (idx < 0) {
      return null;
    }
    //TODO: check slashes here
    return baseUri.substring(0, idx) + myEntry.getPackageDownloadUrl();
  }

  @Override
  protected String getValue(@NotNull String key) {
    return NuGetUtils.getValue(myEntry.getAttributes(), key);
  }
}
