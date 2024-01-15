

package jetbrains.buildServer.nuget.feed.server;

import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.serverSide.parameters.AbstractParameterDescriptionProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 09.02.12 18:55
 */
public class NuGetFeedParametersDescription extends AbstractParameterDescriptionProvider {
  @Override
  public String describe(@NotNull String paramName) {
    if (NuGetServerConstants.FEED_PARAM_PATTERN.matcher(paramName).find()) {
      return "Contains TeamCity provided NuGet feed URL";
    }
    if (paramName.equals(NuGetServerConstants.FEED_REFERENCE_AGENT_API_KEY_PROVIDED)) {
      return "Contains API key to push packages into TeamCity provided NuGet feed";
    }
    return null;
  }
}
