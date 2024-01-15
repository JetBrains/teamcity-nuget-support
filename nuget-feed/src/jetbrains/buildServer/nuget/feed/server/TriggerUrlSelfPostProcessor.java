

package jetbrains.buildServer.nuget.feed.server;

import jetbrains.buildServer.nuget.server.TriggerUrlPostProcessor;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.SBuildType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created 26.06.13 19:09
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class TriggerUrlSelfPostProcessor implements TriggerUrlPostProcessor {
  private final NuGetFeedParametersProvider myProvider;

  public TriggerUrlSelfPostProcessor(@NotNull NuGetFeedParametersProvider provider) {
    myProvider = provider;
  }

  @NotNull
  public String updateTriggerUrl(@NotNull SBuildType buildType, @NotNull String url) {
    if (!ReferencesResolverUtil.mayContainReference(url)) return url;

    final Map<String,String> map = myProvider.getBuildTypeParameters(buildType);
    for (Map.Entry<String, String> e : map.entrySet()) {
      url = url.replace(ReferencesResolverUtil.makeReference(e.getKey()), e.getValue());
    }
    return url;
  }
}
