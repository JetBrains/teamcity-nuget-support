

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.nuget.server.TriggerUrlPostProcessor;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.SBuildType;
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
  private final RootUrlHolder myHolder;

  public TriggerUrlRootPostProcessor(@NotNull RootUrlHolder holder) {
    myHolder = holder;
  }

  @NotNull
  public String updateTriggerUrl(@NotNull SBuildType buildType, @NotNull String source) {
    if (!ReferencesResolverUtil.mayContainReference(source)) return source;
    return source.replace(ReferencesResolverUtil.makeReference(TEAMCITY_SERVER_URL), myHolder.getRootUrl());
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
