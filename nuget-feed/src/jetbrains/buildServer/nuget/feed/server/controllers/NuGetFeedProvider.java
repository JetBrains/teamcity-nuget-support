

package jetbrains.buildServer.nuget.feed.server.controllers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;

/**
 * Manages nuget request handler.
 */
public interface NuGetFeedProvider {
    @Nullable
    NuGetFeedHandler getHandler(@NotNull HttpServletRequest request);
}
