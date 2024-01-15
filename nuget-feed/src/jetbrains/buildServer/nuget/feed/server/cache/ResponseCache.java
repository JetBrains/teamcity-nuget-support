

package jetbrains.buildServer.nuget.feed.server.cache;

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP cache for popular requests.
 */
public interface ResponseCache extends ResponseCacheReset {
  void getOrCompute(@NotNull NuGetFeedData feedData,
                    @NotNull HttpServletRequest request,
                    @NotNull HttpServletResponse response,
                    @NotNull ComputeAction action) throws Exception;

  interface ComputeAction {
    void compute(@NotNull final NuGetFeedData feedData,
                 @NotNull final HttpServletRequest request,
                 @NotNull final HttpServletResponse response) throws Exception;
  }
}
