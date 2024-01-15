

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 15:34
 */
public interface TriggerUpdateChecker {
  /**
   * Called from background thread to check for updates
   *
   * Update is called for different trigger settings and thus there should
   * be nothing cached in instance
   *
   * @return null or StartReason instance to start a build
   * @throws jetbrains.buildServer.buildTriggers.BuildTriggerException
   *          on error
   */
  @Nullable
  BuildStartReason checkChanges(@NotNull PolledTriggerContext context) throws BuildTriggerException;
}
