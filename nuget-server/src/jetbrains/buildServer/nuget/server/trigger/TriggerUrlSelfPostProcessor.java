package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerPropertiesProvider;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created 26.06.13 19:09
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class TriggerUrlSelfPostProcessor implements TriggerUrlPostProcessor {
  private final NuGetServerPropertiesProvider myProvider;

  public TriggerUrlSelfPostProcessor(@NotNull NuGetServerPropertiesProvider provider) {
    myProvider = provider;
  }

  @NotNull
  public String updateTriggerUrl(@NotNull BuildTriggerDescriptor context, @NotNull String url) {
    if (!ReferencesResolverUtil.mayContainReference(url)) return url;

    final Map<String,String> map = myProvider.getProperties();
    for (Map.Entry<String, String> e : map.entrySet()) {
      url = url.replace(ReferencesResolverUtil.makeReference(e.getKey()), e.getValue());
    }
    return url;
  }
}
