

package jetbrains.buildServer.nuget.feed.server.controllers;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetResponseUtil {
  private static final Logger LOG = Logger.getInstance(NuGetResponseUtil.class.getName());

  public static ModelAndView nugetFeedIsDisabled(@NotNull final HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "NuGet Feed server is not enabled in TeamCity server configuration");
    return null;
  }

  public static ModelAndView noImplementationFoundError(@NotNull final HttpServletResponse response) throws IOException {
    final String err = "No available " + NuGetFeedHandler.class + " implementations registered";
    LOG.warn(err);
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
    return null;
  }
}
