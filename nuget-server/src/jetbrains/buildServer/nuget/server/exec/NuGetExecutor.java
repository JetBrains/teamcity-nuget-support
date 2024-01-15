

package jetbrains.buildServer.nuget.server.exec;

import jetbrains.buildServer.nuget.common.auth.PackageSource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:23
 */
public interface NuGetExecutor {
  @NotNull
  <T> T executeNuGet(@NotNull File nugetExePath,
                     @NotNull List<String> arguments,
                     @NotNull Collection<PackageSource> sources,
                     @NotNull NuGetOutputProcessor<T> listener) throws NuGetExecutionException;
}
