

package jetbrains.buildServer.nuget.common.index;

import jetbrains.buildServer.nuget.spec.NuspecFileContent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Evgeniy.Koshkin
 */
public interface NuGetPackageStructureAnalyser {
  void analyseEntry(@NotNull String entryName);
  void analyseNuspecFile(@NotNull NuspecFileContent nuspecContent);
}
