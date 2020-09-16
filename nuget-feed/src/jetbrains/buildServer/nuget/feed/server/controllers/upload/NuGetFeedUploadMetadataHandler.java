package jetbrains.buildServer.nuget.feed.server.controllers.upload;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.nuget.common.PackageExistsException;
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadHandlerContext;
import jetbrains.buildServer.serverSide.RunningBuildEx;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface NuGetFeedUploadMetadataHandler<TContext extends NuGetFeedUploadHandlerContext> {

  void validate(@NotNull final MultipartHttpServletRequest request,
                @NotNull final HttpServletResponse response,
                @NotNull final TContext context,
                @NotNull final RunningBuildEx build,
                @NotNull final String key,
                @NotNull final Map<String, String> metadata) throws PackageExistsException;

  void handleMetadata(@NotNull final MultipartHttpServletRequest request,
                      @NotNull final HttpServletResponse response,
                      @NotNull final TContext context,
                      @NotNull final RunningBuildEx build,
                      @NotNull final String key,
                      @NotNull final Map<String, String> metadata);
}
