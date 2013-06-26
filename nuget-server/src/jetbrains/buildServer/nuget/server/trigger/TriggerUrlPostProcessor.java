package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.serverSide.ServerExtension;
import org.jetbrains.annotations.NotNull;

/**
 * Created 26.06.13 19:01
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface TriggerUrlPostProcessor extends ServerExtension {
  @NotNull
  String updateTriggerUrl(@NotNull BuildTriggerDescriptor context,
                          @NotNull String url);
}
