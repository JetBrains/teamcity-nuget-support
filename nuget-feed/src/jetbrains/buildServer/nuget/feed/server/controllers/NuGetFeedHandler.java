

package jetbrains.buildServer.nuget.feed.server.controllers;

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 17.01.12 17:46
 */
public interface NuGetFeedHandler {

  void handleRequest(@NotNull final NuGetFeedData feedData,
                     @NotNull final HttpServletRequest request,
                     @NotNull final HttpServletResponse response) throws Exception;
}
