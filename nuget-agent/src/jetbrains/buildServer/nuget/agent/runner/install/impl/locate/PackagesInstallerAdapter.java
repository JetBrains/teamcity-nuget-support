

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.util.EventListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 12:15
 */
@EventListenerAdapter
public class PackagesInstallerAdapter implements PackagesInstallerCallback {
  public void onPackagesConfigFound(@NotNull File config, @NotNull File repositoryPath) throws RunBuildException {
  }

  public void onNoPackagesConfigsFound() throws RunBuildException {
  }

  public void onSolutionFileFound(@NotNull File sln, @NotNull File repositoryPath) throws RunBuildException {
  }
}
