

package jetbrains.buildServer.nuget.server.trigger.impl.queue;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 15:49
 */
public class PackageChangesCheckerStarter {
  public PackageChangesCheckerStarter(@NotNull final EventDispatcher<BuildServerListener> events,
                                      @NotNull final PackageChangesCheckerThread thread) {

    events.addListener(new BuildServerAdapter(){
      @Override
      public void serverStartup() {
        thread.startPackagesCheck();
      }

      @Override
      public void serverShutdown() {
        thread.stopPackagesCheck();
      }
    });
  }
}
