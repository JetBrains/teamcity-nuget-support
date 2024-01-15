

package jetbrains.buildServer.nuget.feed.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.build.steps.BuildStepsEditor;
import jetbrains.buildServer.serverSide.impl.build.steps.BuildStartContextBase;
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;


/**
 * Hides tokens for built-in NuGet feed.
 */
public class NuGetFeedPasswordProvider implements PasswordsProvider {

  @NotNull
  @Override
  public Collection<Parameter> getPasswordParameters(@NotNull SBuild build) {
    final List<Parameter> passwords = new ArrayList<>();
    final String feedApiKey = build.getParametersProvider().get(NuGetServerConstants.FEED_REFERENCE_AGENT_API_KEY_PROVIDED);

    if (!StringUtil.isEmptyOrSpaces(feedApiKey)) {
      passwords.add(new SimpleParameter(NuGetServerConstants.FEED_REFERENCE_AGENT_API_KEY_PROVIDED, feedApiKey));
    }

    final BuildPromotionEx buildPromotion = (BuildPromotionEx)build.getBuildPromotion();
    final SBuildStepsCollection runners = buildPromotion.getBuildSettings().getAllBuildRunners();
    if (!(runners instanceof BuildStartContextBase)) return passwords;

    final BuildStepsEditor steps = ((BuildStartContextBase)runners).getRootSteps();
    for (SBuildRunnerDescriptor step : steps.getSteps()) {
      if (!PackagesConstants.PUBLISH_RUN_TYPE.equals(step.getRunType().getType())) continue;

      final String apiKey = step.getParameters().get(PackagesConstants.NUGET_API_KEY);
      if (StringUtil.isEmptyOrSpaces(apiKey)) continue;

      passwords.add(new SimpleParameter(PackagesConstants.NUGET_API_KEY, apiKey));
    }

    return passwords;
  }
}
