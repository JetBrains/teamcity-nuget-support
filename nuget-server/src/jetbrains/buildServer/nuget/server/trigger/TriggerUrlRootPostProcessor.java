

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.ProjectAwareRootUrlResolver;
import jetbrains.buildServer.nuget.server.TriggerUrlPostProcessor;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.agent.AgentRuntimeProperties.TEAMCITY_SERVER_URL;

/**
 * Created 26.06.13 19:02
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class TriggerUrlRootPostProcessor implements TriggerUrlPostProcessor, PositionAware {
  private final ProjectAwareRootUrlResolver myRootUrlResolver;

  public TriggerUrlRootPostProcessor(@NotNull ProjectAwareRootUrlResolver rootUrlResolver) {
    myRootUrlResolver = rootUrlResolver;
  }

  @NotNull
  public String updateTriggerUrl(@NotNull SBuildType buildType, @NotNull String source) {
    if (!ReferencesResolverUtil.mayContainReference(source)) return source;
    // a project may define the root URL with a trailing slash, while the trigger URL continues with an absolute path
    final String rootUrl = StringUtil.removeTailingSlash(myRootUrlResolver.getRootUrlByProjectExternalId(buildType.getProject().getExternalId()));
    return source.replace(ReferencesResolverUtil.makeReference(TEAMCITY_SERVER_URL), rootUrl);
  }

  @NotNull
  @Override
  public String getOrderId() {
    return TriggerUrlRootPostProcessor.class.getName();
  }

  @NotNull
  @Override
  public PositionConstraint getConstraint() {
    return PositionConstraint.last();
  }
}
