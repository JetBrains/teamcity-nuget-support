

package jetbrains.buildServer.nuget.server.runner;

import jetbrains.buildServer.serverSide.RunTypeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 18:52
 */
public class NuGetRunTypesRegistrar {
  public NuGetRunTypesRegistrar(@NotNull final RunTypeRegistry registry,
                                @NotNull final Collection<NuGetRunType> types) {
    for (NuGetRunType type : types) {
      registry.registerRunType(type);
    }
  }
}
