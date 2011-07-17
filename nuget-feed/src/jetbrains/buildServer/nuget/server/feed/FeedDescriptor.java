package jetbrains.buildServer.nuget.server.feed;

import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OEntity;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.QueryInfo;

import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 0:56
 */
public class FeedDescriptor {

  public EntitiesResponse getEntities(@NotNull final List<OEntity> entries,
                                      @NotNull final QueryInfo filters) {
    return new PackagesResponseBase() {

      public List<OEntity> getEntities() {
        return entries;
      }

      public Integer getInlineCount() {
        return null;
      }

      public String getSkipToken() {
        return filters.skipToken;
      }
    };
  }


}
