

package jetbrains.buildServer.nuget.feed.server.odata4j;

import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import org.jetbrains.annotations.NotNull;
import org.odata4j.edm.EdmEntitySet;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesEntitySet {
  @NotNull
  private static final EdmEntitySet.Builder myPackagesEntitySetBuilder
          = new EdmEntitySet.Builder().setName(MetadataConstants.ENTITY_SET_NAME);

  public static EdmEntitySet.Builder getBuilder() {
    return myPackagesEntitySetBuilder;
  }
}
