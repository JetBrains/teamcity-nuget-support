

package jetbrains.buildServer.nuget.server.exec;

import org.jetbrains.annotations.NotNull;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 14.07.11 13:23
*/
public interface NuGetOutputProcessor<T> {
  void onStdOutput(@NotNull String text);
  void onStdError(@NotNull String text);
  void onFinished(int exitCode) throws NuGetExecutionException;

  @NotNull
  T getResult() throws NuGetExecutionException;
}
