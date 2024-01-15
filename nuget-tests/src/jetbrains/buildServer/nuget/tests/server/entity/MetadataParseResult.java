

package jetbrains.buildServer.nuget.tests.server.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 07.01.12 9:49
*/
public final class MetadataParseResult {
  private final Collection<MetadataBeanProperty> myKey;
  private final Collection<MetadataBeanProperty> myData;

  public MetadataParseResult(@NotNull final Collection<MetadataBeanProperty> key,
                             @NotNull final Collection<MetadataBeanProperty> data) {
    myKey = key;
    myData = data;
  }

  @NotNull
  public Collection<MetadataBeanProperty> getKey() {
    return myKey;
  }

  @NotNull
  public Collection<MetadataBeanProperty> getData() {
    return myData;
  }
}
