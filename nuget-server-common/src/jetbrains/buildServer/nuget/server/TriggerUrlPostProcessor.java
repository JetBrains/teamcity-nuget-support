

package jetbrains.buildServer.nuget.server;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.ServerExtension;
import org.jetbrains.annotations.NotNull;

/**
 * Created 26.06.13 19:01
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface TriggerUrlPostProcessor extends ServerExtension {
  @NotNull
  String updateTriggerUrl(@NotNull SBuildType buildType, @NotNull String url);
}
