

package jetbrains.buildServer.nuget.agent.dependencies.impl;

import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 23:00
 */
public interface NuGetPackagesCollectorEx extends NuGetPackagesCollector {
  void removeAllPackages();
}
