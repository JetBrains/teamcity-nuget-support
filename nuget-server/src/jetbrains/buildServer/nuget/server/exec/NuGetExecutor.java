package jetbrains.buildServer.nuget.server.exec;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:23
 */
public interface NuGetExecutor {
  <T> T executeNuGet(@NotNull List<String> arguments,
                     @NotNull NuGetOutputProcessor<T> listener);
}
