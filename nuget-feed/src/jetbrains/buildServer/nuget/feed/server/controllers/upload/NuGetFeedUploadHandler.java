package jetbrains.buildServer.nuget.feed.server.controllers.upload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;

public interface NuGetFeedUploadHandler<TContext extends NuGetFeedUploadHandlerContext> {
  void handleRequest(@NotNull final TContext context,
                     @NotNull final HttpServletRequest request,
                     @NotNull final HttpServletResponse response) throws Exception;
}
