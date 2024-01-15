

package jetbrains.buildServer.nuget.server.exec;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 18:51
 */
public abstract class NuGetOutputProcessorAdapter<T> implements NuGetOutputProcessor<T> {
  protected final Logger LOG = Logger.getInstance(getClass().getName());
  private final String myCommandName;

  protected NuGetOutputProcessorAdapter(@NotNull final String commandName) {
    myCommandName = commandName;
  }

  public void onStdOutput(@NotNull String text) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(text.trim());
    }
  }

  public void onStdError(@NotNull String text) {
    if (!StringUtil.isEmptyOrSpaces(text)) {
      LOG.warn(text.trim());
    }
  }

  public void onFinished(int exitCode) throws NuGetExecutionException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("NuGet " + myCommandName + " command exited with " + exitCode);
    }
    if (exitCode != 0) {
      throw new NuGetExecutionException("Failed to execute NuGet " + myCommandName + " command. Exited code was " + exitCode);
    }
  }
}
